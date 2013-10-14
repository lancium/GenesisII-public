package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.simple.SimpleByteIOTransferer;
import edu.virginia.vcgr.genii.client.lease.ResourceLeaser;

/**
 * This is the final implementation class of a leaser which can lease out byte arrays to be used for
 * ByteIO Buffering.
 * 
 * @author mmm2a
 */
public class ByteIOBufferLeaser extends ResourceLeaser<ByteBuffer>
{
	static private Map<URI, ByteIOBufferLeaser> _leasers;

	static {
		_leasers = new HashMap<URI, ByteIOBufferLeaser>(3);

		ByteIOBufferLeaser eightMegBuffer = new ByteIOBufferLeaser(1024 * 1024 * 8, 4);
		ByteIOBufferLeaser oneMegBuffer = new ByteIOBufferLeaser(1024 * 1024, 4);

		_leasers.put(SimpleByteIOTransferer.TRANSFER_PROTOCOL, eightMegBuffer);
		_leasers.put(DimeByteIOTransferer.TRANSFER_PROTOCOL, oneMegBuffer);
		_leasers.put(MTOMByteIOTransferer.TRANSFER_PROTOCOL, eightMegBuffer);
	}

	private int _bufferSize;

	private ByteIOBufferLeaser(int bufferSize, int numBuffers)
	{
		super(numBuffers);

		_bufferSize = bufferSize;
	}

	@Override
	protected ByteBuffer createNewResource()
	{
		return ByteBuffer.allocate(_bufferSize);
	}

	static public ByteIOBufferLeaser leaser(URI protocol)
	{
		return _leasers.get(protocol);
	}
}