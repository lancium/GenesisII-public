package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.client.lease.ResourceLeaser;

/**
 * This is the final implementation class of a leaser which can lease
 * out byte arrays to be used for ByteIO Buffering.
 * 
 * @author mmm2a
 */
public class ByteIOBufferLeaser extends ResourceLeaser<ByteBuffer>
{
	static final public int BUFFER_SIZE = 1024 * 1024 * 8;
	static final public int NUM_BUFFERS = 4;
	
	/**
	 * The singleton leaser.
	 */
	static private ByteIOBufferLeaser _leaser =
		new ByteIOBufferLeaser();
	
	private ByteIOBufferLeaser()
	{
		super(NUM_BUFFERS);
	}
	
	@Override
	protected ByteBuffer createNewResource()
	{
		return ByteBuffer.allocate(BUFFER_SIZE);
	}
	
	/**
	 * Retrieve the singleton leaser available to the system.
	 * @return
	 */
	static public ByteIOBufferLeaser leaser()
	{
		return _leaser;
	}
}