package edu.virginia.vcgr.fuse.fs.genii;

import java.nio.ByteBuffer;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseFileHandleBadStateException;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import fuse.FuseException;

abstract class FuseFileCommon implements FuseFile
{
	protected abstract void readImpl(long offset, ByteBuffer buffer) 
		throws FuseException;
	protected abstract void writeImpl(long offset, ByteBuffer buffer)
		throws FuseException;
	protected abstract void appendImpl(ByteBuffer buffer)
		throws FuseException;

	protected EndpointReferenceType _target;
	protected GeniiFuseFileSystemContext _fsContext;
	
	private boolean _read;
	private boolean _write;
	private boolean _append;	
	
	FuseFileCommon(EndpointReferenceType target, 
		GeniiFuseFileSystemContext fsContext, 
		boolean read, boolean write, boolean append)
	{
		_target = target;
		_fsContext = fsContext;
		
		_read = read;
		_write = write;
		_append = append;
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		flush();
		close();
	}
	
	@Override
	public void read(long offset, ByteBuffer buffer) throws FuseException
	{
		if (!_read)
			throw new FuseFileHandleBadStateException(
				"Cannot read from file.");
		
		readImpl(offset, buffer);
	}

	@Override
	public void write(long offset, ByteBuffer buffer) throws FuseException
	{
		if (_append)
			appendImpl(buffer);
		else if (_write)
			writeImpl(offset, buffer);
		else
			throw new FuseFileHandleBadStateException(
				"Cannot write to file.");
	}
}