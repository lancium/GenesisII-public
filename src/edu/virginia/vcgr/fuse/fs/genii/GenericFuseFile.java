package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.fuse.exceptions.FuseIllegalAccessException;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import fuse.FuseException;

class GenericFuseFile implements FuseFile
{
	private byte[] _content = null;
	
	GenericFuseFile(byte []content)
	{
		_content = content;
	}
	
	@Override
	public void read(long offset, ByteBuffer buffer) throws FuseException
	{
		long bytesLeft = _content.length - offset;
		if (bytesLeft < 0)
			return;
		
		if (bytesLeft > buffer.remaining())
			bytesLeft = buffer.remaining();
		
		buffer.put(_content, (int)offset, (int)bytesLeft);
	}

	@Override
	public void write(long offset, ByteBuffer buffer) throws FuseException
	{
		throw new FuseIllegalAccessException(
			"Unable to write to a generic entry.");
	}

	@Override
	public void release() throws FuseException
	{
		// do nothing
	}
	
	@Override
	public void close() throws IOException
	{
		// do nothing
	}
	
	@Override
	public void flush() throws FuseException
	{
		// do nothing;
	}
}