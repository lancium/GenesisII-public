package edu.virginia.g3.fsview.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;

public class IOUtils
{
	static final private int DEFAULT_COPY_BLOCK_SIZE = 1024 * 4;

	static public void close(Closeable closeable)
	{
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Throwable cause) {
			}
		}
	}

	static public void copy(InputStream source, OutputStream sink, int blockSize) throws IOException
	{
		byte[] data = new byte[blockSize];
		int read;

		while ((read = source.read(data)) > 0)
			sink.write(data, 0, read);
	}

	static public void copy(InputStream source, OutputStream sink) throws IOException
	{
		copy(source, sink, DEFAULT_COPY_BLOCK_SIZE);
	}

	static public void copy(Reader source, Writer sink, int blockSize) throws IOException
	{
		char[] data = new char[blockSize];
		int read;

		while ((read = source.read(data)) > 0)
			sink.write(data, 0, read);
	}

	static public void copy(Reader source, Writer sink) throws IOException
	{
		copy(source, sink, DEFAULT_COPY_BLOCK_SIZE);
	}

	static public void write(OutputStream out, ByteBuffer buffer) throws IOException
	{
		byte[] data = new byte[buffer.remaining()];
		int pos = buffer.position();
		buffer.get(data);

		try {
			out.write(data);
			data = null;
		} finally {
			if (data != null)
				buffer.position(pos);
		}
	}

	static public void read(InputStream in, ByteBuffer buffer) throws IOException
	{
		byte[] data = new byte[buffer.remaining()];
		int read;

		while (buffer.hasRemaining() && (read = in.read(data, 0, buffer.remaining())) > 0)
			buffer.put(data, 0, read);
	}
}