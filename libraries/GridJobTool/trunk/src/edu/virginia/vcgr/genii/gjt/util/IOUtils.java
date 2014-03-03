package edu.virginia.vcgr.genii.gjt.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class IOUtils {
	static private Logger _logger = Logger.getLogger(IOUtils.class);

	static final private int BUFFER_SIZE = 1024 * 8;

	static public void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] data = new byte[BUFFER_SIZE];
		int read;

		while ((read = in.read(data)) > 0)
			out.write(data, 0, read);
	}

	static public void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Throwable cause) {
				_logger.error("Error trying to close closeable item.", cause);
			}
		}
	}
}