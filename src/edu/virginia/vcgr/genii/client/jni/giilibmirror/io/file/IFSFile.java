package edu.virginia.vcgr.genii.client.jni.giilibmirror.io.file;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public abstract class IFSFile extends IFSResource
{
	protected boolean isAStream = false; 
	
	private boolean _readable;
	private boolean _writeable;
	private boolean _isAppend;
	
	protected final boolean isAppend()
	{
		return _isAppend;
	}
	
	protected abstract byte[] doRead(int length) throws IOException;
	protected abstract int doWrite(byte []data) throws IOException;
	protected abstract int doTruncateAppend(long offset,byte []data) throws IOException;	
	protected abstract void doClose() throws IOException;
	
	public IFSFile(RNSPath path, boolean isReadable,
		boolean isWriteable, boolean isAppend)
	{
		super(path);
		isDirectory = false;
		_readable = isReadable;
		_writeable = isWriteable;
		_isAppend = isAppend;
	}
	
	public void close() throws IOException{
		doClose();		
	}
	
	public abstract long lseek64(long offset) throws IOException;	
	
	public byte[] read(int length) throws IOException
	{
		if (_readable)
			return doRead(length);		
		throw new IOException("File is not open for reading.");
	}
	
	public int write(byte []data) throws IOException
	{
		if (_writeable)
			return doWrite(data);
		throw new IOException("File is not open for writing.");
	}
	
	public int truncateAppend(long offset, byte []data) throws IOException
	{
		if(_writeable)
			return doTruncateAppend(offset, data);
		throw new IOException("File is not open for writing.");
	}
	
	public boolean isStream(){
		return isAStream;
	}
}
