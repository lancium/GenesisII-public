package edu.virginia.vcgr.ogrsh.server.file;

import java.io.IOException;

import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public abstract class AbstractFileDescriptor implements IFileDescriptor
{
	private boolean _readable;
	private boolean _writeable;
	private boolean _isAppend;

	protected final boolean isAppend()
	{
		return _isAppend;
	}

	protected abstract byte[] doRead(int length) throws OGRSHException;

	protected abstract int doWrite(byte[] data) throws OGRSHException;

	public abstract StatBuffer fxstat() throws OGRSHException;

	protected AbstractFileDescriptor(boolean isReadable, boolean isWriteable, boolean isAppend) throws OGRSHException
	{
		_readable = isReadable;
		_writeable = isWriteable;
		_isAppend = isAppend;
	}

	public void close() throws IOException
	{
		// By default, I don't need to do anything.
	}

	public byte[] read(int length) throws OGRSHException
	{
		if (_readable)
			return doRead(length);

		throw new OGRSHException(OGRSHException.EBADF, "File is not open for reading.");
	}

	public int write(byte[] data) throws OGRSHException
	{
		if (_writeable)
			return doWrite(data);

		throw new OGRSHException(OGRSHException.EBADF, "File is not open for writing.");
	}
}
