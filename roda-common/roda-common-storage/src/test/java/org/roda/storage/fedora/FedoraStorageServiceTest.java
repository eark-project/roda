package org.roda.storage.fedora;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.roda.storage.AbstractStorageServiceTest;
import org.roda.storage.Binary;
import org.roda.storage.Container;
import org.roda.storage.ContentPayload;
import org.roda.storage.RandomMockContentPayload;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageTestUtils;
import org.roda.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/fcrepo/spring-test/test-container.xml")
public class FedoraStorageServiceTest extends AbstractStorageServiceTest<FedoraStorageService> {

	static final String PROTOCOL = "http";
	static final String HOSTNAME = "localhost";
	static final int SERVER_PORT = getOpenDoor(9999);
	static final String serverAddress = PROTOCOL + "://" + HOSTNAME + ":" + SERVER_PORT + "/";

	final Logger logger = LoggerFactory.getLogger(FedoraStorageServiceTest.class);

	final FedoraStorageService storage = new FedoraStorageService(serverAddress);

	@AfterClass
	public static void tearDown() throws StorageServiceException {
		FSUtils.deletePath(Paths.get("fcrepo4-data"));
	}

	public static int getOpenDoor(int defaultDoor) {
		int openDoor = defaultDoor;
		try {
			ServerSocket s = new ServerSocket(0);
			openDoor = s.getLocalPort();
			s.close();
		} catch (IOException e) {
			openDoor = defaultDoor;
		}
		System.setProperty("fcrepo.dynamic.test.port", openDoor + "");
		return openDoor;
	}

	@Override
	public void testClassInstantiation() throws StorageServiceException {
		final HttpClient client = HttpClientBuilder.create().setMaxConnPerRoute(Integer.MAX_VALUE)
				.setMaxConnTotal(Integer.MAX_VALUE).build();
		final HttpGet request = new HttpGet(serverAddress);
		HttpResponse response;
		try {
			response = client.execute(request);
			assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Override
	protected FedoraStorageService getStorage() {
		return storage;
	}

	@Override
	public void cleanUp() {
		try {
			for (Container container : storage.listContainers()) {
				storage.deleteContainer(container.getStoragePath());
			}
		} catch (StorageServiceException e) {
			logger.error("Error cleaning up", e);
		}
	}

	@Test
	public void testCreateGetDeleteBinary() throws StorageServiceException, IOException {

		// create container
		final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
		final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
		getStorage().createContainer(containerStoragePath, containerMetadata);

		// 1) create binary
		final StoragePath binaryStoragePath = StorageTestUtils
				.generateRandomResourceStoragePathUnder(containerStoragePath);
		final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
		final ContentPayload payload = new RandomMockContentPayload();
		getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

		// 2) get binary
		Binary binary = getStorage().getBinary(binaryStoragePath);
		assertNotNull(binary);
		assertEquals(binaryStoragePath, binary.getStoragePath());
		assertEquals(binaryMetadata, binary.getMetadata());
		assertFalse(binary.isDirectory());
		assertFalse(binary.isReference());
		testBinaryContent(binary, payload);

		// 3) create binary that already exists
		// XXX Fedora does not handle this, i.e. to create a new
		// datastream when it already exists, very well and therefore catching
		// the already exists exception will no be tested
		try {
			getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

		} catch (StorageServiceException e) {
			e.printStackTrace();
			fail("An exception should not have been thrown while creating a binary that already exists because fedora doesn't support it very well but it happened!");
		}

		// 4) delete binary
		getStorage().deleteResource(binaryStoragePath);

		// 5) test deleted binary
		try {
			getStorage().getBinary(binaryStoragePath);
			fail("An exception should have been thrown while getting a binary that was deleted but it didn't happened!");
		} catch (StorageServiceException e) {
			assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
		}

		// cleanup
		getStorage().deleteContainer(containerStoragePath);
	}

}
