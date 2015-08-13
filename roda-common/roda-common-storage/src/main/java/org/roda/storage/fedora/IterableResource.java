package org.roda.storage.fedora;

import java.io.IOException;
import java.util.Iterator;

import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.roda.storage.ClosableIterable;
import org.roda.storage.Resource;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StoragePath;
import org.roda.storage.fedora.utils.FedoraConversionUtils;
import org.roda.storage.fedora.utils.FedoraUtils;

/**
 * Class that implements {@code Iterable<Resource>} for a particular storage
 * path
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class IterableResource implements ClosableIterable<Resource> {
	private FedoraRepository repository;
	private Iterator<FedoraResource> fedoraResources;

	public IterableResource(FedoraRepository repository, StoragePath storagePath)
			throws StorageServiceException {
		this.repository = repository;
		try {
			fedoraResources = repository
					.getObject(FedoraUtils.createFedoraPath(storagePath))
					.getChildren(null).iterator();
		} catch (ForbiddenException e) {
			throw new StorageServiceException(e.getMessage(),
					StorageServiceException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageServiceException(e.getMessage(),
					StorageServiceException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageServiceException(e.getMessage(),
					StorageServiceException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageServiceException(e.getMessage(),
					StorageServiceException.BAD_REQUEST, e);
		}
	}

	@Override
	public Iterator<Resource> iterator() {
		return new ResourceIterator(repository, fedoraResources);
	}

	public class ResourceIterator implements Iterator<Resource> {
		private Iterator<FedoraResource> fedoraResources;

		public ResourceIterator(FedoraRepository repository,
				Iterator<FedoraResource> fedoraResources) {
			this.fedoraResources = fedoraResources;
		}

		@Override
		public boolean hasNext() {
			if (fedoraResources == null) {
				return false;
			}
			return fedoraResources.hasNext();

		}

		@Override
		public Resource next() {
			try {
				FedoraResource resource = fedoraResources.next();
				if (resource instanceof FedoraDatastream) {
					return FedoraConversionUtils
							.fedoraDatastreamToBinary((FedoraDatastream) resource);
				} else {
					return FedoraConversionUtils.fedoraObjectToDirectory(
							repository.getRepositoryUrl(),
							(FedoraObject) resource);
				}
			} catch (StorageServiceException e) {
				return null;
			}
		}

		@Override
		public void remove() {
		}

	}

	@Override
	public void close() throws IOException {
		// do nothing
	}
}
