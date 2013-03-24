package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.client.lease.LeaseableResource;
import edu.virginia.vcgr.genii.client.lease.LeaseeAgreement;

public class AppendableBuffer implements Closeable
{
	/**
	 * Any IO exceptions that may occur during a flush. This is stored so that if the exception
	 * occurs during an asynchronous operation, it can be thrown later when a synchronous one
	 * occurs.
	 */
	private IOException _ioe = null;

	/**
	 * An internal lock object on which synchronization will be done.
	 */
	private Object _lockObject = new Object();

	/**
	 * The current leased byte array (if any).
	 */
	private LeaseableResource<ByteBuffer> _lease = null;

	/** The leaser to use to obtain new buffers */
	private ByteIOBufferLeaser _leaser;

	/** A resolver capable of flushing bytes back to a sink */
	private AppendResolver _resolver;

	/**
	 * Ensure that the given file offset can be written to the buffer. If the current state of the
	 * buffer does not allow this, then flush what we currently have and reset.
	 * 
	 * @throws IOException
	 */
	private void ensure() throws IOException
	{
		ByteBuffer buffer;

		if (_lease == null) {
			_lease = _leaser.obtainLease(new LeaseeAgreementImpl());
			buffer = _lease.resource();
			buffer.clear();
		} else
			buffer = _lease.resource();

		if (!buffer.hasRemaining()) {
			flush();
			buffer.clear();
		}
	}

	/**
	 * Create a new writable buffer.
	 * 
	 * @param leaser
	 *            The leaser to use when obtaining new byte array leases.
	 * @param resolver
	 *            The resolver to use when a buffer needs to be flushed to the sink.
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
		synchronized (_lockObject) {
			if (_lease != null) {
				try {
					flush();
				} catch (Throwable cause) {
				}
				_lease.cancel();
			}

			_lease = null;
		}
	}

	public void append(ByteBuffer source) throws IOException
	{
		synchronized (_lockObject) {
			if (_ioe != null) {
				IOException ioe = _ioe;
				_ioe = null;
				throw ioe;
			}

			while (source.hasRemaining()) {
				ensure();
				ByteBuffer buffer = _lease.resource().slice();
				ByteBuffer sourceCopy = source.slice();
				if (buffer.remaining() < sourceCopy.remaining())
					sourceCopy.limit(buffer.remaining());
				buffer.put(sourceCopy);
				source.position(source.position() + sourceCopy.position());
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
		synchronized (_lockObject) {
			if (_ioe != null) {
				IOException ioe = _ioe;
				_ioe = null;
				throw ioe;
			}

			if (_lease != null) {
				ByteBuffer buffer = _lease.resource();
				buffer.flip();
				if (buffer.hasRemaining())
					_resolver.append(buffer);
				buffer.clear();
			}
		}
	}

	/**
	 * The implementation of the lease agreement for this writable buffer.
	 * 
	 * @author mmm2a
	 */
	private class LeaseeAgreementImpl implements LeaseeAgreement<ByteBuffer>
	{
		@Override
		public LeaseableResource<ByteBuffer> relinquish(LeaseableResource<ByteBuffer> lease)
		{
			LeaseableResource<ByteBuffer> ret;

			synchronized (_lockObject) {
				ret = _lease;
				try {
					flush();
				} catch (IOException ioe) {
					_ioe = ioe;
				}
				_lease = null;
				return ret;
			}
		}
	}
}