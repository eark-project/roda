package org.roda.storage;

import java.util.Iterator;

/**
 * Storage Service related and independent utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public final class StorageServiceUtils {

	/**
	 * Private empty constructor
	 * */
	private StorageServiceUtils() {

	}

	/**
	 * Move resources from a given storage service/storage path to another
	 * storage service/storage path
	 * 
	 * @param fromService
	 *            source storage service
	 * @param fromStoragePath
	 *            source storage path
	 * @param toService
	 *            destination storage service
	 * @param toStoragePath
	 *            destination storage path
	 * @param rootEntity
	 *            class of the root entity
	 * */
	public static void moveBetweenStorageServices(StorageService fromService,
			StoragePath fromStoragePath, StorageService toService,
			StoragePath toStoragePath, Class<? extends Entity> rootEntity)
			throws StorageServiceException {
		copyOrMoveBetweenStorageServices(fromService, fromStoragePath,
				toService, toStoragePath, rootEntity, false);
	}

	/**
	 * Copy resources from a given "storage service/storage path" to another
	 * "storage service/storage path"
	 * 
	 * @param fromService
	 *            source storage service
	 * @param fromStoragePath
	 *            source storage path
	 * @param toService
	 *            destination storage service
	 * @param toStoragePath
	 *            destination storage path
	 * @param rootEntity
	 *            class of the root entity
	 * */
	public static void copyBetweenStorageServices(StorageService fromService,
			StoragePath fromStoragePath, StorageService toService,
			StoragePath toStoragePath, Class<? extends Entity> rootEntity)
			throws StorageServiceException {
		copyOrMoveBetweenStorageServices(fromService, fromStoragePath,
				toService, toStoragePath, rootEntity, true);
	}

	private static void copyOrMoveBetweenStorageServices(
			StorageService fromService, StoragePath fromStoragePath,
			StorageService toService, StoragePath toStoragePath,
			Class<? extends Entity> rootEntity, boolean copy)
			throws StorageServiceException {
		if (Container.class.isAssignableFrom(rootEntity)) {
			Container container = fromService.getContainer(fromStoragePath);
			toService.createContainer(toStoragePath, container.getMetadata());
			Iterator<Resource> childResourcesIterator = fromService
					.listResourcesUnderContainer(fromStoragePath).iterator();
			iterateAndCopyOrMoveResourcesRecursivelly(fromService,
					fromStoragePath, toService, toStoragePath,
					childResourcesIterator, copy);
			if (!copy) {
				fromService.deleteContainer(fromStoragePath);
			}
		} else if (Directory.class.isAssignableFrom(rootEntity)) {
			Directory directory = fromService.getDirectory(fromStoragePath);
			toService.createDirectory(toStoragePath, directory.getMetadata());
			Iterator<Resource> childResourcesIterator = fromService
					.listResourcesUnderDirectory(fromStoragePath).iterator();
			iterateAndCopyOrMoveResourcesRecursivelly(fromService,
					fromStoragePath, toService, toStoragePath,
					childResourcesIterator, copy);
			if (!copy) {
				fromService.deleteResource(fromStoragePath);
			}
		} else {
			Binary binary = fromService.getBinary(fromStoragePath);
			// FIXME how to set this?
			boolean asReference = false;
			toService.createBinary(toStoragePath, binary.getMetadata(),
					binary.getContent(), asReference);
			if (!copy) {
				fromService.deleteResource(fromStoragePath);
			}
		}
	}

	private static void iterateAndCopyOrMoveResourcesRecursivelly(
			StorageService fromService, StoragePath fromStoragePath,
			StorageService toService, StoragePath toStoragePath,
			Iterator<Resource> childResourcesIterator, boolean copy)
			throws StorageServiceException {
		while (childResourcesIterator.hasNext()) {
			Resource child = childResourcesIterator.next();
			if (copy) {
				toService.copy(
						fromService,
						child.getStoragePath(),
						extractToStoragePathChild(fromStoragePath,
								child.getStoragePath(), toStoragePath));
			} else {
				toService.move(
						fromService,
						child.getStoragePath(),
						extractToStoragePathChild(fromStoragePath,
								child.getStoragePath(), toStoragePath));
			}
		}
	}

	private static StoragePath extractToStoragePathChild(
			StoragePath fromStoragePath, StoragePath fromStoragePathChild,
			StoragePath toStoragePath) throws StorageServiceException {
		String path = fromStoragePathChild.asString();
		path = path.replaceFirst(fromStoragePath.asString(),
				toStoragePath.asString());

		return DefaultStoragePath.parse(path);
	}
}
