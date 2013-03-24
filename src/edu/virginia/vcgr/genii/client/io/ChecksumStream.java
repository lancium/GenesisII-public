package edu.virginia.vcgr.genii.client.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ChecksumStream extends FilterInputStream
{
	private Checksum _checksummer;

	public ChecksumStream(InputStream underlyingStream)
	{
		this(underlyingStream, new CRC32());
	}

	public ChecksumStream(InputStream underlyingStream, Checksum checksummer)
	{
		super(underlyingStream);

		_checksummer = checksummer;
		_checksummer.reset();
	}

	public boolean markSupported()
	{
		return false;
	}

	public int read() throws IOException
	{
		int ret = super.read();
		if (ret >= 0)
			_checksummer.update(ret);
		return ret;
	}

	public int read(byte[] b) throws IOException
	{
		int read = super.read(b);
		if (read > 0)
			_checksummer.update(b, 0, read);
		return read;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		int read = super.read(b, off, len);
		if (read > 0)
			_checksummer.update(b, off, read);
		return read;
	}

	public void reset() throws IOException
	{
		throw new IOException("Marking not supported on ChecksumStream.");
	}

	public long skip(long n) throws IOException
	{
		throw new IOException("Skip not supported in ChecksumStream.");
	}

	public long getChecksum()
	{
		return _checksummer.getValue();
	}
}