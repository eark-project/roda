/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ApacheDS;
import org.roda.core.common.LdapUtility;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.LogEntryParameter;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RODAObject;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.User;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexServiceTest {

  private static Path basePath;
  private static Path indexPath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static ApacheDS apacheDS;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("modelTests");
    logPath = basePath.resolve("log");
    Files.createDirectory(logPath);
    indexPath = Files.createTempDirectory("indexTests");
    storage = new FileStorageService(basePath);
    model = new ModelService(storage);

    System.setProperty("roda.home", basePath.toString());
    RodaCoreFactory.instantiateTest();

    // Configure Solr
    URL solrConfigURL = IndexServiceTest.class.getResource("/config/index/solr.xml");
    Path solrConfigPath = Paths.get(solrConfigURL.toURI());
    Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
    Path aipSchema = indexPath.resolve("aip");
    Files.createDirectories(aipSchema);
    Files.createFile(aipSchema.resolve("core.properties"));

    Path solrHome = Paths.get(IndexServiceTest.class.getResource("/config/index/").toURI());
    System.setProperty("solr.data.dir", indexPath.toString());
    System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
    System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
    System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
    System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
    System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
    System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
    System.setProperty("solr.data.dir.jobreport", indexPath.resolve("jobreport").toString());
    System.setProperty("solr.data.dir.members", indexPath.resolve("members").toString());
    System.setProperty("solr.data.dir.othermetadata", indexPath.resolve("othermetadata").toString());
    System.setProperty("solr.data.dir.sip", indexPath.resolve("sip").toString());
    System.setProperty("solr.data.dir.job", indexPath.resolve("job").toString());
    System.setProperty("solr.data.dir.file", indexPath.resolve("file").toString());
    
    // start embedded solr
    final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

    index = new IndexService(solr, model);

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    // set of properties that will most certainly be in a default roda
    // installation
    Configuration rodaConfig = setAndRetrieveRodaProperties();

    // start ApacheDS
    apacheDS = new ApacheDS();
    Files.createDirectories(basePath.resolve("ldapData"));
    Path ldapConfigs = Paths.get(IndexServiceTest.class.getResource("/config/ldap/").toURI());
    String ldapHost = rodaConfig.getString("ldap.host", "localhost");
    int ldapPort = rodaConfig.getInt("ldap.port", 10389);
    String ldapPeopleDN = rodaConfig.getString("ldap.peopleDN");
    String ldapGroupsDN = rodaConfig.getString("ldap.groupsDN");
    String ldapRolesDN = rodaConfig.getString("ldap.rolesDN");
    String ldapAdminDN = rodaConfig.getString("ldap.adminDN");
    String ldapAdminPassword = rodaConfig.getString("ldap.adminPassword");
    String ldapPasswordDigestAlgorithm = rodaConfig.getString("ldap.passwordDigestAlgorithm");
    List<String> ldapProtectedUsers = RodaUtils.copyList(rodaConfig.getList("ldap.protectedUsers"));
    List<String> ldapProtectedGroups = RodaUtils.copyList(rodaConfig.getList("ldap.protectedGroups"));
    apacheDS.initDirectoryService(ldapConfigs, basePath.resolve("ldapData"), ldapAdminPassword);
    apacheDS.startServer(new LdapUtility(ldapHost, ldapPort, ldapPeopleDN, ldapGroupsDN, ldapRolesDN, ldapAdminDN,
      ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers, ldapProtectedGroups), 10389);
    for (User user : UserUtility.getLdapUtility().getUsers(new Filter())) {
      model.addUser(user, false, true);
    }
    for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
      model.addGroup(group, false, true);
    }

    logger.debug("Running model test under storage: " + basePath);
  }

  private static Configuration setAndRetrieveRodaProperties() {
    Configuration rodaConfig = new BaseConfiguration();
    rodaConfig.addProperty("ldap.host", "localhost");
    rodaConfig.addProperty("ldap.port", "10389");
    rodaConfig.addProperty("ldap.peopleDN", "ou=users\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.groupsDN", "ou=groups\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.rolesDN", "ou=roles\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.adminDN", "uid=admin\\,ou=system");
    rodaConfig.addProperty("ldap.adminPassword", "secret");
    rodaConfig.addProperty("ldap.passwordDigestAlgorithm", "MD5");
    rodaConfig.addProperty("ldap.protectedUsers",
      Arrays.asList("admin", "guest", "roda-ingest-task", "roda-wui", "roda-disseminator"));
    rodaConfig.addProperty("ldap.protectedGroups",
      Arrays.asList("administrators", "archivists", "producers", "users", "guests"));
    return rodaConfig;
  }

  @AfterClass
  public static void tearDown() throws Exception {
    apacheDS.stop();
    FSUtils.deletePath(basePath);
    FSUtils.deletePath(indexPath);
  }

  @Test
  public void testAIPIndexCreateDelete() throws ParseException, NotFoundException, RequestNotValidException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    // Retrieve, count and list AIP
    final AIP indexedAIP = index.retrieve(AIP.class, aipId);
    assertEquals(aip, indexedAIP);

    final long countAIP = index.count(AIP.class, null);
    assertEquals(1, countAIP);

    final IndexResult<AIP> aipList = index.find(AIP.class, null, null, new Sublist(0, 10), null);
    assertEquals(1, aipList.getTotalCount());
    assertEquals(1, aipList.getLimit());
    assertEquals(0, aipList.getOffset());
    assertEquals(1, aipList.getResults().size());
    assertEquals(aip, aipList.getResults().get(0));

    // Retrieve, count and list SDO
    final SimpleDescriptionObject sdo = index.retrieve(SimpleDescriptionObject.class, aipId);
    assertEquals(aip.getId(), sdo.getId());
    assertEquals(aip.isActive(), RODAObject.STATE_ACTIVE.equals(sdo.getState()));
    assertEquals(aip.getParentId(), sdo.getParentID());
    assertEquals(aip.getDateCreated(), sdo.getCreatedDate());
    assertEquals(aip.getDateModified(), sdo.getLastModifiedDate());

    final IndexResult<SimpleDescriptionObject> sdos = index.find(SimpleDescriptionObject.class, null, null,
      new Sublist(0, 10), null);
    assertEquals(1, sdos.getTotalCount());
    assertEquals(1, sdos.getLimit());
    assertEquals(0, sdos.getOffset());
    assertEquals(1, sdos.getResults().size());

    final SimpleDescriptionObject sdoFromList = sdos.getResults().get(0);
    assertEquals(aip.getId(), sdoFromList.getId());
    assertEquals(aip.isActive(), RODAObject.STATE_ACTIVE.equals(sdoFromList.getState()));
    assertEquals(aip.getParentId(), sdoFromList.getParentID());
    assertEquals(aip.getDateCreated(), sdoFromList.getCreatedDate());
    assertEquals(aip.getDateModified(), sdoFromList.getLastModifiedDate());

    assertEquals(sdo, sdoFromList);
    assertEquals("fonds", sdo.getLevel());
    assertEquals("My example", sdo.getTitle());
    assertEquals("This is a very nice example", sdo.getDescription());
    assertEquals(RodaUtils.parseDate("0001-01-01T00:00:00.000+0000"), sdo.getDateInitial());
    assertEquals(RodaUtils.parseDate("0002-01-01T00:00:00.000+0000"), sdo.getDateFinal());

    // Retrieve, count and list SRO
    String rep1Id = aip.getRepresentationIds().get(0);
    Representation rep1 = index.retrieve(Representation.class, aipId, rep1Id);
    assertEquals(rep1Id, rep1.getId());

    Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.SRO_AIP_ID, aipId));
    IndexResult<Representation> sros = index.find(Representation.class, filterParentTheAIP, null, new Sublist(0, 10),
      null);
    assertEquals(aip.getRepresentationIds().size(), sros.getTotalCount());

    List<String> sro_IDs = new ArrayList<>();
    for (Representation sro : sros.getResults()) {
      sro_IDs.add(sro.getId());
    }

    assertThat(sro_IDs, Matchers.contains(aip.getRepresentationIds().toArray()));

    /*
     * filterMimetype.add(new SimpleFilterParameter(RodaConstants.SRFM_MIMETYPE,
     * CorporaConstants.TEXT_XML));
     * assertEquals(index.find(SimpleEventPreservationMetadata.class,
     * filterMimetype, null, new Sublist(0, 10)).getTotalCount(),1L);
     */

    /*
     * SimpleRepresentationPreservationMetadata srpm =
     * index.retrieveSimpleRepresentationPreservationMetadata(aipId,
     * CorporaConstants.REPRESENTATION_1_ID,CorporaConstants.
     * REPRESENTATION_PREMIS_XML); assertEquals(srpm.getAipId(), aipId);
     * assertEquals(srpm.getFileId(),CorporaConstants.
     * REPRESENTATION_PREMIS_XML); Filter filterFileId = new Filter();
     * filterFileId.add(new SimpleFilterParameter(RodaConstants.SRPM_FILE_ID,
     * CorporaConstants.REPRESENTATION_PREMIS_XML));
     * assertEquals(""+index.countSimpleRepresentationPreservationMetadata(
     * filterFileId),""+1L);
     * assertEquals(index.find(SimpleEventPreservationMetadata.class,
     * filterFileId, null, new Sublist(0, 10)).getTotalCount(),1L);
     */
    model.deleteAIP(aipId);
    try {
      index.retrieve(AIP.class, aipId);
      fail("AIP deleted but yet it was retrieved");
    } catch (NotFoundException e) {
      // do nothing as it was the expected exception
    } catch (RODAException e) {
      fail("AIP was deleted and therefore a " + NotFoundException.class.getName()
        + " should have been thrown instead of a " + e.getClass().getName());
    }

    try {
      index.retrieve(SimpleDescriptionObject.class, aipId);
      fail("AIP was deleted but yet its descriptive metadata was retrieved");
    } catch (NotFoundException e) {
      // do nothing as it was the expected exception
    } catch (RODAException e) {
      fail("AIP was deleted and therefore a " + NotFoundException.class.getName()
        + " should have been thrown instead of a " + e.getClass().getName());
    }
  }

  @Test
  public void testAIPIndexCreateDelete2() throws ParseException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    final String aipId = UUID.randomUUID().toString();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_3));

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_ID, aipId));
    SimpleDescriptionObject sdo = index.find(SimpleDescriptionObject.class, filter, null, new Sublist(0, 10), null)
      .getResults().get(0);
    Calendar calInitial = Calendar.getInstance();
    calInitial.setTime(sdo.getDateInitial());
    assertEquals(calInitial.get(Calendar.YEAR), CorporaConstants.YEAR_1213);
    Calendar calFinal = Calendar.getInstance();
    calFinal.setTime(sdo.getDateFinal());
    assertEquals(calFinal.get(Calendar.YEAR), CorporaConstants.YEAR_2003);

  }

  @Test
  public void testAIPUpdate() throws NotFoundException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    final AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath);

    final AIP indexedAIP = index.retrieve(AIP.class, aipId);
    assertEquals(updatedAIP, indexedAIP);

    model.deleteAIP(aipId);
  }

  @Test
  public void testListCollections() throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    long sdoCount = index.count(SimpleDescriptionObject.class, SimpleDescriptionObject.FONDS_FILTER);
    assertEquals(1, sdoCount);

    final IndexResult<SimpleDescriptionObject> sdos = index.find(SimpleDescriptionObject.class,
      SimpleDescriptionObject.FONDS_FILTER, null, new Sublist(0, 10), null);

    assertEquals(1, sdos.getLimit());
    assertEquals(CorporaConstants.SOURCE_AIP_ID, sdos.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testSubElements() throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, CorporaConstants.SOURCE_AIP_ID));

    long sdoCount = index.count(SimpleDescriptionObject.class, filter);
    assertEquals(1, sdoCount);

    final IndexResult<SimpleDescriptionObject> sdos = index.find(SimpleDescriptionObject.class, filter, null,
      new Sublist(0, 10), null);

    assertEquals(1, sdos.getLimit());
    assertEquals(CorporaConstants.OTHER_AIP_ID, sdos.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testGetAncestors() throws NotFoundException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    SimpleDescriptionObject sdo = index.retrieve(SimpleDescriptionObject.class, CorporaConstants.OTHER_AIP_ID);
    List<SimpleDescriptionObject> ancestors = index.getAncestors(sdo);
    assertThat(ancestors, Matchers
      .hasItem(Matchers.<SimpleDescriptionObject> hasProperty("id", Matchers.equalTo(CorporaConstants.SOURCE_AIP_ID))));
  }

  @Test
  public void testGetElementWithoutParentId() throws RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.SDO_LEVEL, "fonds"));
    filter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
    IndexResult<SimpleDescriptionObject> findDescriptiveMetadata = index.find(SimpleDescriptionObject.class, filter,
      null, new Sublist(), null);

    assertNotNull(findDescriptiveMetadata);
    assertThat(findDescriptiveMetadata.getResults(), Matchers.hasSize(1));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testGetLogEntriesCount()
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    // cleaning up action log entries on index (if any)
    index.deleteAllActionLog();

    LogEntry entry = new LogEntry();
    entry.setActionComponent("Action");
    entry.setActionMethod("Method");
    entry.setAddress("Address");
    entry.setDatetime(new Date());
    entry.setDuration(10L);
    entry.setId("ID");
    entry.setRelatedObjectID("Related");
    entry.setUsername("Username");
    List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID"));
    assertThat(index.count(LogEntry.class, filterDescription), Matchers.is(1L));

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID2"));
    assertThat(index.count(LogEntry.class, filterDescription2), Matchers.is(0L));
  }

  @Test
  public void testFindLogEntry()
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    LogEntry entry = new LogEntry();
    entry.setActionComponent(RodaConstants.LOG_ACTION_COMPONENT);
    entry.setActionMethod("Method");
    entry.setAddress("address");
    entry.setDatetime(new Date());
    entry.setDuration(10L);
    entry.setId("id");
    entry.setRelatedObjectID("related");
    entry.setUsername("username");
    List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id"));

    IndexResult<LogEntry> entries = index.find(LogEntry.class, filterDescription, null, new Sublist(), null);
    assertEquals(entries.getTotalCount(), 1);
    assertEquals(entries.getResults().get(0).getActionComponent(), RodaConstants.LOG_ACTION_COMPONENT);

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id2"));

    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, filterDescription2, null, new Sublist(), null);
    assertEquals(entries2.getTotalCount(), 0);
  }

  @Test
  public void testReindexLogEntry()
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    Long number = 10L;

    for (int i = 0; i < number; i++) {
      LogEntry entry = new LogEntry();
      entry.setId("ID" + i);
      entry.setActionComponent("ACTION:" + i);
      entry.setActionMethod("Method:" + i);
      entry.setAddress("ADDRESS");
      entry.setDatetime(new Date());
      entry.setDuration(i);
      entry.setRelatedObjectID("RELATED:" + i);
      entry.setUsername("USER:" + i);
      List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
      parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
      parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
      entry.setParameters(parameters);
      model.addLogEntry(entry, logPath, false);
    }
    model.findOldLogsAndMoveThemToStorage(logPath, null);
    index.reindexActionLogs();
    Filter f1 = new Filter();
    f1.add(new SimpleFilterParameter(RodaConstants.LOG_ACTION_COMPONENT, "ACTION:0"));
    IndexResult<LogEntry> entries1 = index.find(LogEntry.class, f1, null, new Sublist(0, 10), null);
    assertThat(entries1.getTotalCount(), Matchers.is(1L));
    Filter f2 = new Filter();
    f2.add(new SimpleFilterParameter(RodaConstants.LOG_ADDRESS, "ADDRESS"));
    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, f2, null, new Sublist(0, 10), null);
    assertThat(entries2.getTotalCount(), Matchers.is(number));
  }

  @Test
  public void testReindexAIP() throws ParseException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    for (int i = 0; i < 10; i++) {
      final String aipId = UUID.randomUUID().toString();
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), false);
    }

    index.reindexAIPs();
    long count = index.count(AIP.class, new Filter());
    assertEquals(count, 10L);

  }

  @Test
  public void indexMembers() throws AlreadyExistsException, GenericException, RequestNotValidException {
    Set<String> groups = new HashSet<String>();
    groups.add("administrators");
    Set<String> roles = new HashSet<String>();
    roles.add("browse");

    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        User user = new User();
        user.setActive(true);
        user.setAllGroups(groups);
        user.setAllRoles(roles);
        user.setDirectGroups(groups);
        user.setDirectRoles(roles);
        user.setEmail("mail@example.com");
        user.setGuest(false);
        user.setId("USER" + i);
        user.setName("NAMEUSER" + i);
        user.setFullName("NAMEUSER" + i);
        model.addUser(user, true, true);
      } else {
        Group group = new Group();
        group.setActive(true);
        group.setAllGroups(groups);
        group.setAllRoles(roles);
        group.setDirectGroups(groups);
        group.setDirectRoles(roles);
        group.setId("GROUP" + i);
        group.setName("NAMEGROUP" + i);
        group.setFullName("NAMEGROUP" + i);
        model.addGroup(group, true, true);
      }
    }

    Filter filterUSER1 = new Filter();
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEUSER0"));
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
    assertThat(index.count(RODAMember.class, filterUSER1), Matchers.is(1L));

    Filter filterGroup = new Filter();
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEGROUP1"));
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "false"));
    assertThat(index.count(RODAMember.class, filterGroup), Matchers.is(1L));

  }
}
