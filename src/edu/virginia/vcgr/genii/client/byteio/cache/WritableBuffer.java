package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.Closeable;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.lease.LeaseableResource;
import edu.virginia.vcgr.genii.client.lease.LeaseeAgreement;

public class WritableBuffer implements Closeable
{
	private IOException _ioe = null;
	private Object _lockObject = new Object();
	
	private long _blockOffsetInFile = -1L;
	private int _nextWrite = -1;
	private LeaseableResource<byte[]> _lease = null;
	private ByteIOBufferLeaser _leaser;
	private WriteResolver _resolver;
	
	private void ensure(long fileOffset) throws IOException
	{
		if (_lease == null)
			_lease = _leaser.obtainLease(new LeaseeAgreementImpl());
		
		byte[] buffer = _lease.resource();
		
		if (_nextWrite != fileOffset || (_nextWrite >= (buffer.length)))
		{
			flush();
			_nextWrite = 0;
			_blockOffsetInFile = fileOffset;
		}
	}
	
	public WritableBuffer(ByteIOBufferLeaser leaser, WriteResolver resolver)
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
			flush();
		}
	}
	
	public void write(long fileOffset, byte []source,
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
				ensure(fileOffset);
				byte []buffer = _lease.resource();
				int space = buffer.length - _nextWrite;
				if (space > length)
					space = length;
				System.arraycopy(source, sourceOffset, 
					buffer, _nextWrite, space);
				length -= space;
				sourceOffset += space;
				fileOffset += space;
				_nextWrite += space;
			}
		}
	}
	
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
					_resolver.write(_blockOffsetInFile, buffer, 0, _nextWrite);
				
				_blockOffsetInFile = -1L;
				_nextWrite = -1;
			}
		}
	}
	
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
				return ret;
			}
		}
	}
}
