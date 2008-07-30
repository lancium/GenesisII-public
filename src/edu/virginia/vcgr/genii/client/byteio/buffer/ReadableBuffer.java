package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;

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
	
	/** The number of valid bytes currently contained in the buffer. */
	private int _validSize = -1;
	
	/** The currently leased buffer (if any) or null */
	private LeaseableResource<byte[]> _lease = null;
	
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
		byte[] buffer = _lease.resource();
		
		if ((_blockOffsetInFile < 0) || (fileOffset < _blockOffsetInFile) || 
			((_blockOffsetInFile + _validSize) <= fileOffset))
		{
			_validSize = _resolver.read(fileOffset, buffer, 0, buffer.length);
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
			_validSize = -1;
			_blockOffsetInFile = -1L;
		}
	}
	
	/**
	 * Read a section of bytes from the file into an array.
	 * 
	 * @param fileOffset The offset in the target file at which to begin
	 * reading.
	 * @param destination The destination byte buffer to fill in.
	 * @param destinationOffset The offset within the destination byte buffer
	 * at which to begin filling in.
	 * @param length The number of bytes to read.
	 * @return The number of bytes read.  This can be a short read, but will
	 * be 0 or -1 if there are no bytes left in the target file.
	 * 
	 * @throws IOException
	 */
	public int read(long fileOffset, byte []destination, 
		int destinationOffset, int length) throws IOException
	{
		synchronized(_lockObject)
		{
			ensure(fileOffset);
			int blockOffset = (int)(fileOffset - _blockOffsetInFile);
			int blockLeft = _validSize - blockOffset;
			if (blockLeft <= 0)
				return 0;
			
			if (blockLeft < length)
				length = blockLeft;
			System.arraycopy(_lease.resource(), blockOffset,
				destination, destinationOffset, length);
			return length;
		}
	}
	
	/**
	 * The implementation of the lease agreement for this readable buffer.
	 * 
	 * @author mmm2a
	 */
	private class LeaseeAgreementImpl implements LeaseeAgreement<byte[]>
	{
		@Override
		public LeaseableResource<byte[]> relinquish(
				LeaseableResource<byte[]> lease)
		{
			synchronized(_lockObject)
			{
				LeaseableResource<byte[]> ret = _lease;
				_lease = null;
				_blockOffsetInFile = -1L;
				_validSize = -1;
				return ret;
			}
		}
	}
}