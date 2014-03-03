package edu.virginia.vcgr.genii.client.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class LoadFileResource {
	private String _resourcePath;

	public LoadFileResource(String resourcePath) {
		_resourcePath = resourcePath;
	}

	public InputStream open() {
		return GenesisClassLoader.classLoaderFactory().getResourceAsStream(
				_resourcePath);
	}

	public String toString() {
		StringWriter writer = new StringWriter();
		InputStream in = null;
		InputStreamReader reader = null;
		char[] buffer = new char[1024 * 4];

		try {
			in = open();
			if (in != null) {
				reader = new InputStreamReader(in);
				int read;

				while ((read = reader.read(buffer)) > 0)
					writer.write(buffer, 0, read);

				writer.close();
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to read resource.", ioe);
		} finally {
			StreamUtils.close(reader);
			StreamUtils.close(in);
		}

		return writer.toString();
	}
}