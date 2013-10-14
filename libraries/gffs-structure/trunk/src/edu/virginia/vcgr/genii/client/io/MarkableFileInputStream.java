package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class MarkableFileInputStream extends InputStream
{
	private long _markPos = -1L;
	private File _source;
	private RandomAccessFile _raf;

	private void reopen() throws FileNotFoundException
	{
		if (_raf == null) {
			_markPos = 0L;
			_raf = new RandomAccessFile(_source, "r");
		}
	}

	public MarkableFileInputStream(File source) throws FileNotFoundException
	{
		_source = source;
		_raf = new RandomAccessFile(source, "r");
	}

	public MarkableFileInputStream(String filename) throws FileNotFoundException
	{
		this(new File(filename));
	}

	@Override
	synchronized public void close() throws IOException
	{
		_raf.close();
		_raf = null;
	}

	@Override
	synchronized public int read() throws IOException
	{
		reopen();
		return _raf.read();
	}

	@Override
	synchronized public int read(byte[] b) throws IOException
	{
		reopen();
		return _raf.read(b);
	}

	@Override
	synchronized public int read(byte[] b, int off, int len) throws IOException
	{
		reopen();
		return _raf.read(b, off, len);
	}

	@Override
	synchronized public long skip(long n) throws IOException
	{
		reopen();
		_raf.seek(_raf.getFilePointer() + n);
		return n;
	}

	@Override
	synchronized public void mark(int readlimit)
	{
		try {
			if (_raf == null)
				throw new RuntimeException("Stream is closed.");

			_markPos = _raf.getFilePointer();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to mark position.", ioe);
		}
	}

	@Override
	public boolean markSupported()
	{
		return true;
	}

	@Override
	synchronized public void reset() throws IOException
	{
		reopen();
		if (_markPos < 0)
			throw new IOException("Mark not set.");

		_raf.seek(_markPos);
		_markPos = -1L;
	}
}