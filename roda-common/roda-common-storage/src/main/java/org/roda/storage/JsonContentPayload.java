package org.roda.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JsonContentPayload implements ContentPayload {
	private String content;
	private Path contentPath;
	public JsonContentPayload(String content) {
		this.content = content;
	}

	@Override
	public InputStream createInputStream() throws IOException {
		return new ByteArrayInputStream(content.getBytes());
	}

	@Override
	public void writeToPath(Path path) throws IOException {
		Files.copy(createInputStream(), path,
				StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public URI getURI() throws IOException, UnsupportedOperationException {
		if (contentPath == null) {
			contentPath = Files.createTempFile("content", ".json");
			writeToPath(contentPath);
		}
		return contentPath.toUri();
	}

}