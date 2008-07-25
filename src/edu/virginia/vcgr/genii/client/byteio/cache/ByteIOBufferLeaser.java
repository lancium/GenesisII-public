package edu.virginia.vcgr.genii.client.byteio.cache;

import edu.virginia.vcgr.genii.client.lease.ResourceLeaser;

public class ByteIOBufferLeaser extends ResourceLeaser<byte[]>
{
	static final public int BUFFER_SIZE = 1024 * 1024;
	static final public int NUM_BUFFERS = 4;
	
	static private ByteIOBufferLeaser _leaser =
		new ByteIOBufferLeaser();
	
	private ByteIOBufferLeaser()
	{
		super(NUM_BUFFERS);
	}
	
	@Override
	protected byte[] createNewResource()
	{
		return new byte[BUFFER_SIZE];
	}
	
	static public ByteIOBufferLeaser leaser()
	{
		return _leaser;
	}
}