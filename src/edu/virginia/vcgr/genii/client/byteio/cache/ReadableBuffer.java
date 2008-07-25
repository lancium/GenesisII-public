package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.Closeable;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.lease.LeaseableResource;
import edu.virginia.vcgr.genii.client.lease.LeaseeAgreement;

public class ReadableBuffer implements Closeable
{
	private Object _lockObject = new Object();
	
	private long _blockOffsetInFile = -1L;
	private int _validSize = -1;
	private LeaseableResource<byte[]> _lease = null;
	private ByteIOBufferLeaser _leaser;
	private ReadResolver _resolver;
	
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
		}
	}
	
	public int read(long fileOffset, byte []destination, 
		int destinationOffset, int length) throws IOException
	{
		synchronized(_lockObject)
		{
			ensure(fileOffset);
			int blockOffset = (int)(fileOffset - _blockOffsetInFile);
			int blockLeft = _validSize - blockOffset;
			if (blockLeft < length)
				length = blockLeft;
			System.arraycopy(_lease.resource(), blockOffset,
				destination, destinationOffset, length);
			return length;
		}
	}
	
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