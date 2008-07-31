package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.lease.LeaseableResource;
import edu.virginia.vcgr.genii.client.lease.LeaseeAgreement;

public class AppendableBuffer implements Closeable
{
	/**
	 * Any IO exceptions that may occur during a flush.  This is stored
	 * so that if the exception occurs during an asynchronous operation, it
	 * can be thrown later when a synchronous one occurs.
	 */
	private IOException _ioe = null;
	
	/**
	 * An internal lock object on which synchronization will be done.
	 */
	private Object _lockObject = new Object();
	
	/**
	 * The location (within the byte array buffer) at which we expect the next
	 * write to occur.  If the next write doesn't line up with this index, then
	 * we flush what we have and start over.
	 */
	private int _nextWrite = -1;
	
	/**
	 * The current leased byte array (if any).
	 */
	private LeaseableResource<byte[]> _lease = null;
	
	/** The leaser to use to obtain new buffers */
	private ByteIOBufferLeaser _leaser;
	
	/** A resolver capable of flushing bytes back to a sink */
	private AppendResolver _resolver;
	
	/**
	 * Ensure that the given file offset can be written to the buffer.  If
	 * the current state of the buffer does not allow this, then flush what
	 * we currently have and reset.
	 *  
	 * @throws IOException
	 */
	private void ensure() throws IOException
	{
		if (_lease == null)
			_lease = _leaser.obtainLease(new LeaseeAgreementImpl());
		
		byte[] buffer = _lease.resource();
		
		if (_nextWrite >= (buffer.length))
		{
			flush();
			_nextWrite = 0;
		}
	}
	
	/**
	 * Create a new writable buffer.
	 * 
	 * @param leaser The leaser to use when obtaining new byte array leases.
	 * @param resolver The resolver to use when a buffer needs to be flushed to
	 * the sink.
	 */
	public AppendableBuffer(ByteIOBufferLeaser leaser, AppendResolver resolver)
	{
		_leaser = leaser;
		_resolver = resolver;
	}
	
	@Override
	protected void finalize() throws IOException
	{
		close();
	}
	
	@Override
	public void close() throws IOException
	{
		synchronized(_lockObject)
		{
			if (_lease != null)
			{
				try { flush(); } catch (Throwable cause) {}
				_lease.cancel();
			}
			
			_nextWrite = -1;
			_lease = null;
		}
	}
	
	/**
	 * Write a given set of bytes to the buffer (or target file).
	 * 
	 * @param fileOffset The offset within the target sink at which to begin
	 * writing.
	 * @param source The source array of bytes to write.
	 * @param sourceOffset THe offset within the source array at which to begin
	 * writing.
	 * @param length THe number of bytes to write.
	 * 
	 * @throws IOException
	 */
	public void append(byte []source,
		int sourceOffset, int length) throws IOException
	{
		synchronized(_lockObject)
		{
			if (_ioe != null)
			{
				IOException ioe = _ioe;
				_ioe = null;
				throw ioe;
			}
			
			while (length > 0)
			{
				ensure();
				byte []buffer = _lease.resource();
				int space = buffer.length - _nextWrite;
				if (space > length)
					space = length;
				System.arraycopy(source, sourceOffset, 
					buffer, _nextWrite, space);
				length -= space;
				sourceOffset += space;
				_nextWrite += space;
			}
		}
	}
	
	/**
	 * Flush the current buffered bytes to the sink.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException
	{
		synchronized(_lockObject)
		{
			if (_ioe != null)
			{
				IOException ioe = _ioe;
				_ioe = null;
				throw ioe;
			}
			
			if (_lease != null)
			{
				byte []buffer = _lease.resource();
				if (_nextWrite > 0)
					_resolver.append(buffer, 0, _nextWrite);
				
				_nextWrite = -1;
			}
		}
	}
	
	/**
	 * The implementation of the lease agreement for this writable buffer.
	 * 
	 * @author mmm2a
	 */
	private class LeaseeAgreementImpl implements LeaseeAgreement<byte[]>
	{
		@Override
		public LeaseableResource<byte[]> relinquish(
				LeaseableResource<byte[]> lease)
		{
			LeaseableResource<byte[]> ret;
			
			synchronized(_lockObject)
			{
				ret = _lease;
				try
				{
					flush();
				}
				catch (IOException ioe)
				{
					_ioe = ioe;
				}
				_lease = null;
				_nextWrite = -1;
				return ret;
			}
		}
	}
}