package edu.virginia.vcgr.genii.client.gfs;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSBadFileHandleException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSFileHandleBadStateException;

abstract class GeniiOpenFile implements Closeable
{
	private boolean _read;
	private boolean _write;
	private boolean _append;
	private boolean _closed = false;
	
	protected abstract void closeImpl() throws IOException;
	protected abstract void readImpl(long offset, ByteBuffer target)
		throws FSException;
	protected abstract void writeImpl(long offset, ByteBuffer source)
		throws FSException;
	protected abstract void appendImpl(ByteBuffer source)
		throws FSException;
	
	protected GeniiOpenFile(boolean canRead, boolean canWrite, 
		boolean isAppend)
	{
		_read = canRead;
		_write = canWrite;
		_append = isAppend;
	}
	
	@Override
	synchronized final public void close() throws IOException
	{
		try
		{
			if (!_closed)
				closeImpl();
		}
		finally
		{
			_closed = true;
		}
	}
	
	void flush() throws FSException
	{
		// Nothing to do.
	}
	
	final public void read(long offset, ByteBuffer target) throws FSException
	{
		if (_closed)
			throw new FSBadFileHandleException("The file is closed.");
		
		if (!_read)
			throw new FSFileHandleBadStateException("Cannot read from file.");
		
		readImpl(offset, target);
	}
	
	final public void write(long offset, ByteBuffer source) throws FSException
	{
		if (_closed)
			throw new FSBadFileHandleException("The file is closed.");
		
		if (_append)
			appendImpl(source);
		else if (_write)
			writeImpl(offset, source);
		else
			throw new FSFileHandleBadStateException("Cannot write to file.");
	}
}