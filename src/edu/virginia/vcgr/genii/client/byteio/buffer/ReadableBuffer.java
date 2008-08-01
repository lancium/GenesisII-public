package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.client.lease.LeaseableResource;
import edu.virginia.vcgr.genii.client.lease.LeaseeAgreement;

/**
 * A buffer object that can use leased by arrays to buffer or cache ByteIO
 * reads.
 * 
 * @author mmm2a
 */
public class ReadableBuffer implements Closeable
{
	/**
	 * An internal lock object on which synchronization will be done.
	 */
	private Object _lockObject = new Object();
	
	/** The current offset within the real file at which the current buffer
	 * of bytes starts.
	 */
	private long _blockOffsetInFile = -1L;
	
	/** The currently leased buffer (if any) or null */
	private LeaseableResource<ByteBuffer> _lease = null;
	
	/** The leaser to use to obtain new buffers */
	private ByteIOBufferLeaser _leaser;
	
	/** A resolver capable of filling in buffered bytes */
	private ReadResolver _resolver;
	
	/**
	 * Ensure that the current buffer contains the file offset indicated (if
	 * at all possible).
	 * 
	 * @param fileOffset The file offset that we want to ensure is available
	 * @throws IOException
	 */
	private void ensure(long fileOffset) throws IOException
	{
		if (_lease == null)
			_lease = _leaser.obtainLease(new LeaseeAgreementImpl());
		ByteBuffer buffer = _lease.resource();
		
		if ((_blockOffsetInFile < 0) || (fileOffset < _blockOffsetInFile) || 
			((_blockOffsetInFile + buffer.limit()) <= fileOffset))
		{
			buffer.clear();
			_resolver.read(fileOffset, buffer);
			buffer.flip();
			_blockOffsetInFile = fileOffset;
		}
	}
	
	/**
	 * Create a new readable buffer.
	 * 
	 * @param leaser The leaser to use to obtain new byte buffers.
	 * @param resolver The resolver to use to fill in byte buffers.
	 */
	public ReadableBuffer(ByteIOBufferLeaser leaser, ReadResolver resolver)
	{
		_resolver = resolver;
		_leaser = leaser;
	}

	@Override
	protected void finalize()
	{
		close();
	}
	
	@Override
	public void close()
	{
		synchronized(_lockObject)
		{
			if (_lease != null)
				_lease.cancel();
			_lease = null;
			_blockOffsetInFile = -1L;
		}
	}
	
	public void read(long fileOffset, ByteBuffer destination) throws IOException
	{
		synchronized(_lockObject)
		{
			ensure(fileOffset);
			ByteBuffer buffer = _lease.resource();
			int blockOffset = (int)(fileOffset - _blockOffsetInFile);
			buffer.position(blockOffset);
			buffer = buffer.slice();
			if (buffer.remaining() <= 0)
				return;
			
			if (buffer.remaining() > destination.remaining())
				buffer.limit(destination.remaining());
			
			destination.put(buffer);
		}
	}
	
	/**
	 * The implementation of the lease agreement for this readable buffer.
	 * 
	 * @author mmm2a
	 */
	private class LeaseeAgreementImpl implements LeaseeAgreement<ByteBuffer>
	{
		@Override
		public LeaseableResource<ByteBuffer> relinquish(
				LeaseableResource<ByteBuffer> lease)
		{
			synchronized(_lockObject)
			{
				LeaseableResource<ByteBuffer> ret = _lease;
				_lease = null;
				_blockOffsetInFile = -1L;
				return ret;
			}
		}
	}
}