package org.morgan.dpage;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class StreamUtils {
	static void close(Closeable closeable) {
		if (closeable != null)
			try {
				closeable.close();
			} catch (Throwable cause) {
			}
	}

	static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[1024 * 8];
		int read;

		while ((read = in.read(data)) > 0) {
			out.write(data, 0, read);
		}
	}
}