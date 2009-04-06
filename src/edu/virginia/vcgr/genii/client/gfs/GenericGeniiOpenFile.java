package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSIllegalAccessException;

class GenericGeniiOpenFile extends GeniiOpenFile
{
	private ByteBuffer _content;
	
	GenericGeniiOpenFile(String[] path, ByteBuffer content,
		boolean canRead, boolean canWrite, boolean isAppend)
	{
		super(path, canRead, canWrite, isAppend);
		_content = content;
	}
	
	@Override
	protected void appendImpl(ByteBuffer source) throws FSException
	{
		throw new FSIllegalAccessException(
			"Unable to write to a generic entry.");
	}

	@Override
	protected void closeImpl() throws IOException
	{
		// do nothing
	}

	@Override
	synchronized protected void readImpl(long offset, ByteBuffer target) 
		throws FSException
	{
		long bytesLeft = _content.remaining() - offset;
		if (bytesLeft <= 0)
			return;
		
		if (bytesLeft > target.remaining())
			bytesLeft = target.remaining();
		
		// do copy of bytes left
		ByteBuffer source = _content.duplicate();
		source.position((int)offset);
		source.limit((int)(offset + bytesLeft));
		target.put(source);
	}

	@Override
	protected void writeImpl(long offset, ByteBuffer source) throws FSException
	{
		throw new FSIllegalAccessException(
			"Unable to write to a generic entry.");
	}
}