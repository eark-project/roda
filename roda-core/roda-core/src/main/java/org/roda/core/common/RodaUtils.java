/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.tuple.Triple;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class RodaUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaUtils.class);

  /** Private empty constructor */
  private RodaUtils() {

  }

  public static String dateToString(Date date) {
    String ret;
    if (date != null) {
      SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
      ret = iso8601DateFormat.format(date);
    } else {
      ret = null;
    }

    return ret;
  }

  public static Date parseDate(String date) throws ParseException {
    Date ret;
    if (date != null) {
      SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
      ret = iso8601DateFormat.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static Map<String, Object> copyMap(Object object) {
    if (!(object instanceof Map)) {
      return null;
    }
    Map<?, ?> map = (Map<?, ?>) object;
    Map<String, Object> temp = new HashMap<String, Object>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() instanceof String) {
        temp.put((String) entry.getKey(), entry.getValue());
      } else {
        return null;
      }
    }
    return temp;
  }

  public static List<String> copyList(Object object) {
    if (!(object instanceof List)) {
      return new ArrayList<String>();
    }
    List<?> list = (List<?>) object;
    List<String> temp = new ArrayList<String>();
    for (Object ob : list) {
      if (ob instanceof String) {
        temp.add((String) ob);
      } else if (ob == null) {
        temp.add(null);
      } else {
        return new ArrayList<String>();
      }
    }
    return temp;
  }

  public static Map<String, List<String>> parseYmlPropertiesFile(File file) throws FileNotFoundException {
    Map<String, List<String>> properties = new HashMap<String, List<String>>();

    Yaml yaml = new Yaml();
    Map<String, Object> map = RodaUtils.copyMap(yaml.load(new FileInputStream(file)));
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      properties.put(entry.getKey(), RodaUtils.copyList(entry.getValue()));
    }

    return properties;
  }

  /**
   * INFO 20160711 this method does not cache stylesheet related resources
   */
  public static void applyStylesheet(Reader xsltReader, Reader fileReader, Map<String, String> parameters,
    Writer result) throws IOException, TransformerException {

    TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
    factory.setURIResolver(new RodaURIResolver());
    Source xsltSource = new StreamSource(xsltReader);
    Transformer transformer = factory.newTransformer(xsltSource);
    for (Entry<String, String> parameter : parameters.entrySet()) {
      transformer.setParameter(parameter.getKey(), (String) parameter.getValue());
    }
    try {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setEntityResolver(new RodaEntityResolver());
      InputSource source = new InputSource(fileReader);
      Source text = new SAXSource(xmlReader, source);
      transformer.transform(text, new StreamResult(result));
    } catch (SAXException se) {
      LOGGER.error(se.getMessage(), se);
    }
  }

  private static Processor PROCESSOR = new Processor(false);

  private static LoadingCache<Triple<String, String, String>, XsltExecutable> CACHE = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<Triple<String, String, String>, XsltExecutable>() {

      @Override
      public XsltExecutable load(Triple<String, String, String> key) throws Exception {
        String basePath = key.getLeft();
        String metadataType = key.getMiddle();
        String metadataVersion = key.getRight();
        return createMetadataTransformer(basePath, metadataType, metadataVersion);
      }

    });

  public static Reader applyMetadataStylesheet(Binary binary, String basePath, String metadataType,
    String metadataVersion, Map<String, String> parameters) throws GenericException {
    Reader descMetadataReader = null;

    try {
      descMetadataReader = new InputStreamReader(new BOMInputStream(binary.getContent().createInputStream()));

      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setEntityResolver(new RodaEntityResolver());
      InputSource source = new InputSource(descMetadataReader);
      Source text = new SAXSource(xmlReader, source);

      XsltExecutable xsltExecutable = CACHE.get(Triple.of(basePath, metadataType, metadataVersion));

      XsltTransformer transformer = xsltExecutable.load();
      CharArrayWriter transformerResult = new CharArrayWriter();

      transformer.setSource(text);
      transformer.setDestination(PROCESSOR.newSerializer(transformerResult));

      for (Entry<String, String> parameter : parameters.entrySet()) {
        QName qName = new QName(parameter.getKey());
        XdmValue xdmValue = new XdmAtomicValue(parameter.getValue());
        transformer.setParameter(qName, xdmValue);
      }

      transformer.transform();

      return new CharArrayReader(transformerResult.toCharArray());

    } catch (IOException | SAXException | ExecutionException | SaxonApiException e) {
      LOGGER.error(e.getMessage(), e);
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath()
        + " metadata type " + metadataType + " and version " + metadataVersion, e);
    } finally {
      IOUtils.closeQuietly(descMetadataReader);
    }
  }

  protected static XsltExecutable createMetadataTransformer(String basePath, String metadataType,
    String metadataVersion) throws SaxonApiException, GenericException {
    InputStream transformerStream = null;

    try {
      // get xslt from metadata type and version if defined
      if (metadataType != null) {
        String lowerCaseMetadataType = metadataType.toLowerCase();
        if (metadataVersion != null) {
          String lowerCaseMetadataTypeWithVersion = lowerCaseMetadataType + RodaConstants.METADATA_VERSION_SEPARATOR
            + metadataVersion;
          transformerStream = RodaCoreFactory
            .getConfigurationFileAsStream(basePath + lowerCaseMetadataTypeWithVersion + ".xslt");
        }
        if (transformerStream == null) {
          transformerStream = RodaCoreFactory.getConfigurationFileAsStream(basePath + lowerCaseMetadataType + ".xslt");
        }
      }

      // fallback
      if (transformerStream == null) {
        // TODO change plain to default
        transformerStream = RodaCoreFactory.getConfigurationFileAsStream(basePath + "plain.xslt");
      }

      if (transformerStream == null) {
        throw new GenericException("Could not find stylesheet nor fallback at basePath=" + basePath + ", metadataType="
          + metadataType + ", metadataVersion=" + metadataVersion);
      }

      XsltCompiler compiler = PROCESSOR.newXsltCompiler();
      compiler.setURIResolver(new RodaURIResolver());
      // compiler.setSchemaAware(false);
      return compiler.compile(new StreamSource(transformerStream));

    } finally {
      IOUtils.closeQuietly(transformerStream);
    }
  }

  public static void indentXML(Reader input, Writer output) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    StreamSource source = new StreamSource(input);
    StreamResult result = new StreamResult(output);
    transformer.transform(source, result);
  }

  public static String indentXML(String xml) throws GenericException {
    Reader input = new StringReader(xml);
    Writer output = new StringWriter();
    try {
      indentXML(input, output);
    } catch (TransformerException e) {
      LOGGER.warn("Could not indent XML", e);
      return xml;
    }
    return output.toString();
  }

  public static long getPathSize(Path startPath) throws IOException {
    final AtomicLong size = new AtomicLong(0);

    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        size.addAndGet(attrs.size());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        // Skip folders that can't be traversed
        return FileVisitResult.CONTINUE;
      }
    });

    return size.get();
  }

}
