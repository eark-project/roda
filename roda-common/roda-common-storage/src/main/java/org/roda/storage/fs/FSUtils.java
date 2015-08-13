package org.roda.storage.fs;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.roda.storage.ClosableIterable;
import org.roda.storage.Container;
import org.roda.storage.ContentPayload;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultContainer;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StoragePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.common.RodaConstants;

/**
 * File System related utility class
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class FSUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FSUtils.class);

	/**
	 * Private empty constructor
	 */
	private FSUtils() {

	}

	/**
	 * Moves a directory/file from one path to another
	 * 
	 * @param sourcePath
	 *            source path
	 * @param targetPath
	 *            target path
	 * @param replaceExisting
	 *            true if the target directory/file should be replaced if it
	 *            already exists; false otherwise
	 * 
	 */
	public static void move(final Path sourcePath, final Path targetPath, boolean replaceExisting)
			throws StorageServiceException {

		// check if we can replace existing
		if (!replaceExisting && Files.exists(targetPath)) {
			throw new StorageServiceException("Cannot copy because target path already exists: " + targetPath,
					StorageServiceException.ALREADY_EXISTS);
		}

		// ensure parent directory exists or can be created
		try {
			Files.createDirectories(targetPath.getParent());
		} catch (IOException e) {
			throw new StorageServiceException("Error while creating target directory parent folder",
					StorageServiceException.INTERNAL_SERVER_ERROR, e);
		}

		CopyOption[] copyOptions = replaceExisting ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING }
				: new CopyOption[] {};

		if (Files.isDirectory(sourcePath)) {
			try {
				Files.move(sourcePath, targetPath, copyOptions);
			} catch (IOException e) {
				throw new StorageServiceException("Error while moving directory from one path to another",
						StorageServiceException.INTERNAL_SERVER_ERROR, e);
			}
		} else {
			try {
				Files.move(sourcePath, targetPath, copyOptions);
				FSYamlMetadataUtils.moveMetadata(sourcePath, targetPath, replaceExisting);
			} catch (IOException e) {
				throw new StorageServiceException("Error while copying one file into another",
						StorageServiceException.INTERNAL_SERVER_ERROR, e);
			}
		}

	}

	/**
	 * Copies a directory/file from one path to another
	 * 
	 * @param sourcePath
	 *            source path
	 * @param targetPath
	 *            target path
	 * @param replaceExisting
	 *            true if the target directory/file should be replaced if it
	 *            already exists; false otherwise
	 */
	public static void copy(final Path sourcePath, final Path targetPath, boolean replaceExisting)
			throws StorageServiceException {

		// check if we can replace existing
		if (!replaceExisting && Files.exists(targetPath)) {
			throw new StorageServiceException("Cannot copy because target path already exists: " + targetPath,
					StorageServiceException.ALREADY_EXISTS);
		}

		// ensure parent directory exists or can be created
		try {
			Files.createDirectories(targetPath.getParent());
		} catch (IOException e) {
			throw new StorageServiceException("Error while creating target directory parent folder",
					StorageServiceException.INTERNAL_SERVER_ERROR, e);
		}

		if (Files.isDirectory(sourcePath)) {
			try {
				Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
							throws IOException {
						Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
							throws IOException {
						Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new StorageServiceException("Error while copying one directory into another",
						StorageServiceException.INTERNAL_SERVER_ERROR, e);
			}
		} else {
			try {

				CopyOption[] copyOptions = replaceExisting ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING }
						: new CopyOption[] {};
				Files.copy(sourcePath, targetPath, copyOptions);
				FSYamlMetadataUtils.copyMetadata(sourcePath, targetPath, replaceExisting);
			} catch (IOException e) {
				throw new StorageServiceException("Error while copying one file into another",
						StorageServiceException.INTERNAL_SERVER_ERROR, e);
			}

		}

	}

	/**
	 * Deletes a directory/file
	 * 
	 * @param path
	 *            path to the directory/file that will be deleted. in case of a
	 *            directory, if not empty, everything in it will be deleted as
	 *            well. in case of a file, if metadata associated to it exists,
	 *            it will be deleted as well.
	 */
	public static void deletePath(Path path) throws StorageServiceException {
		if (path == null) {
			return;
		}

		try {
			Files.delete(path);

			// if it is a file, try to delete associated metadata (if it exists)
			if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
				FSYamlMetadataUtils.deleteMetadata(path);
			}
		} catch (NoSuchFileException e) {
			throw new StorageServiceException("Could not delete path", StorageServiceException.NOT_FOUND, e);
		} catch (DirectoryNotEmptyException e) {
			try {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}

				});
			} catch (IOException e1) {
				throw new StorageServiceException("Could not delete entity",
						StorageServiceException.INTERNAL_SERVER_ERROR, e1);
			}
		} catch (IOException e) {
			throw new StorageServiceException("Could not delete entity", StorageServiceException.INTERNAL_SERVER_ERROR,
					e);
		}
	}

	/**
	 * Get path
	 * 
	 * @param basePath
	 *            base path
	 * @param storagePath
	 *            storage path, related to base path, that one wants to resolve
	 */
	public static Path getEntityPath(Path basePath, StoragePath storagePath) {
		Path resourcePath = basePath.resolve(storagePath.asString());
		return resourcePath;
	}

	/**
	 * List content of the certain folder
	 * 
	 * @param basePath
	 *            base path
	 * @param path
	 *            relative path to base path
	 */
	public static ClosableIterable<Resource> listPath(final Path basePath, final Path path)
			throws StorageServiceException {
		ClosableIterable<Resource> resourceIterable;
		try {
			final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path,
					FSYamlMetadataUtils.PATH_FILTER);
			final Iterator<Path> pathIterator = directoryStream.iterator();
			resourceIterable = new ClosableIterable<Resource>() {

				@Override
				public Iterator<Resource> iterator() {
					return new Iterator<Resource>() {

						@Override
						public boolean hasNext() {
							return pathIterator.hasNext();
						}

						@Override
						public Resource next() {
							Path next = pathIterator.next();
							Resource ret;
							try {
								ret = convertPathToResource(basePath, next);
							} catch (StorageServiceException | NoSuchElementException e) {
								LOGGER.error("Error while list path " + basePath + " while parsing resource " + next,
										e);
								ret = null;
							}

							return ret;
						}

					};
				}

				@Override
				public void close() throws IOException {
					directoryStream.close();
				}
			};

		} catch (NoSuchFileException e) {
			throw new StorageServiceException("Could not list contents of entity because it doesn't exist: " + path,
					StorageServiceException.NOT_FOUND, e);
		} catch (IOException e) {
			throw new StorageServiceException("Could not list contents of entity at: " + path,
					StorageServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return resourceIterable;
	}

	/**
	 * List containers
	 * 
	 * @param basePath
	 *            base path
	 */
	public static ClosableIterable<Container> listContainers(final Path basePath) throws StorageServiceException {
		ClosableIterable<Container> containerIterable;
		try {
			final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(basePath,
					FSYamlMetadataUtils.PATH_FILTER);
			final Iterator<Path> pathIterator = directoryStream.iterator();
			containerIterable = new ClosableIterable<Container>() {

				@Override
				public Iterator<Container> iterator() {
					return new Iterator<Container>() {

						@Override
						public boolean hasNext() {
							return pathIterator.hasNext();
						}

						@Override
						public Container next() {
							Path next = pathIterator.next();
							Container ret;
							try {
								ret = convertPathToContainer(basePath, next);
							} catch (StorageServiceException | NoSuchElementException e) {
								LOGGER.error("Error while listing containers, while parsing resource " + next, e);
								ret = null;
							}

							return ret;
						}

					};
				}

				@Override
				public void close() throws IOException {
					directoryStream.close();
				}
			};

		} catch (IOException e) {
			throw new StorageServiceException("Could not list contents of entity at: " + basePath,
					StorageServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return containerIterable;
	}

	/**
	 * Converts a path into a resource
	 * 
	 * @param basePath
	 *            base path
	 * @param path
	 *            relative path to base path
	 */
	public static Resource convertPathToResource(Path basePath, Path path) throws StorageServiceException {
		Resource resource;

		// TODO support binary reference

		if (!Files.exists(path)) {
			throw new StorageServiceException("Cannot find file or directory at " + path,
					StorageServiceException.NOT_FOUND);
		}

		// storage path
		Path relativePath = basePath.relativize(path);
		StoragePath storagePath = DefaultStoragePath.parse(relativePath.toString());

		// metadata
		Map<String, Set<String>> metadata = FSYamlMetadataUtils.readMetadata(path);

		// construct
		if (Files.isDirectory(path)) {
			resource = new DefaultDirectory(storagePath, metadata);
		} else {
			ContentPayload content = new FSPathContentPayload(path);
			long sizeInBytes;
			try {
				sizeInBytes = Files.size(path);
				Map<String, String> contentDigest = FSUtils.obtainContentDigest(metadata);
				resource = new DefaultBinary(storagePath, metadata, content, sizeInBytes, false, contentDigest);
			} catch (IOException e) {
				throw new StorageServiceException("Could not get file size",
						StorageServiceException.INTERNAL_SERVER_ERROR, e);
			}
		}
		return resource;
	}

	private static Map<String, String> obtainContentDigest(Map<String, Set<String>> metadata) {
		Map<String, String> digest = new HashMap<String, String>();

		if (metadata != null) {
			Set<String> digestValue = metadata.get(RodaConstants.STORAGE_META_DIGEST_SHA1);

			if (digestValue != null && digestValue.size() == 1) {
				digest.put(RodaConstants.STORAGE_META_DIGEST_SHA1, digestValue.iterator().next());
			}
		}

		return digest;
	}

	/**
	 * Converts a path into a container
	 * 
	 * @param basePath
	 *            base path
	 * @param path
	 *            relative path to base path
	 */
	public static Container convertPathToContainer(Path basePath, Path path) throws StorageServiceException {
		Container resource;

		// storage path
		Path relativePath = basePath.relativize(path);
		StoragePath storagePath = DefaultStoragePath.parse(relativePath.toString());

		// metadata
		Map<String, Set<String>> metadata = FSYamlMetadataUtils.readMetadata(path);

		// construct
		if (Files.isDirectory(path)) {
			resource = new DefaultContainer(storagePath, metadata);
		} else {
			throw new StorageServiceException("A file is not a container!",
					StorageServiceException.INTERNAL_SERVER_ERROR);
		}
		return resource;
	}

	/**
	 * Method for computing a file content digest (a.k.a. hash) using
	 * <code>MD5</code> algorithm.
	 * 
	 * @param path
	 *            file which digest will be computed
	 */
	public static String computeContentDigestMD5(Path path) throws StorageServiceException {
		return computeContentDigest(path, RodaConstants.MD5);
	}

	/**
	 * Method for computing a file content digest (a.k.a. hash) using
	 * <code>SHA-1</code> algorithm.
	 * 
	 * @param path
	 *            file which digest will be computed
	 */
	public static String computeContentDigestSHA1(Path path) throws StorageServiceException {
		return computeContentDigest(path, RodaConstants.SHA1);
	}

	private static String computeContentDigest(Path path, String algorithm) throws StorageServiceException {

		try {
			final int bufferSize = 1073741824;
			final FileChannel fc = FileChannel.open(path);
			final long size = fc.size();
			final MessageDigest hash = MessageDigest.getInstance(algorithm);
			long position = 0;
			while (position < size) {
				final MappedByteBuffer data = fc.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(size, bufferSize));
				if (!data.isLoaded()) {
					data.load();
				}
				hash.update(data);
				position += data.limit();
				if (position >= size) {
					break;
				}
			}

			byte[] mdbytes = hash.digest();
			StringBuilder hexString = new StringBuilder();

			for (int i = 0; i < mdbytes.length; i++) {
				hexString.append(Integer.toHexString((0xFF & mdbytes[i])));
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException | IOException e) {
			throw new StorageServiceException(
					"Cannot compute content digest for " + path + " using algorithm " + algorithm,
					StorageServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Method for computing one or more file content digests (a.k.a. hash's)
	 * 
	 * @param path
	 *            file which digests will be computed
	 */
	public static Map<String, String> generateContentDigest(Path path) throws StorageServiceException {
		Map<String, String> digest = new HashMap<String, String>(1);

		String pathDigest = computeContentDigestSHA1(path);
		digest.put(RodaConstants.STORAGE_META_DIGEST_SHA1, pathDigest);

		return digest;
	}
}
