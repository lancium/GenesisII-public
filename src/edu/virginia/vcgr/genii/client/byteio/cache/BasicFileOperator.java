package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.Closeable;
import java.io.IOException;

public class BasicFileOperator implements Closeable
{
	private ReadResolver _readResolver;
	private WriteResolver _writeResolver;
	
	private ByteIOBufferLeaser _leaser;
	
	private ReadableBuffer _readBuffer = null;
	private WritableBuffer _writeBuffer = null;
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver,
		WriteResolver writeResolver, boolean truncate) throws IOException
	{
		_leaser = leaser;
		
		if (readResolver == null)
			readResolver = new NonReadableReadResolver();
		
		if (writeResolver == null)
			writeResolver = new NonWritableWriteResolver();
		
		_readResolver = readResolver;
		_writeResolver = writeResolver;
		
		if (truncate)
			_writeResolver.truncate(0L);
	}
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver)
		throws IOException
	{
		this(leaser, readResolver, null, false);
	}
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, WriteResolver writeResolver,
		boolean truncate) throws IOException
	{
		this(leaser, null, writeResolver, truncate);
	}
	
	@Override
	protected void finalize() throws IOException
	{
		close();
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_readBuffer != null)
			_readBuffer.close();
		if (_writeBuffer != null)
			_writeBuffer.close();
		
		_readBuffer = null;
		_writeBuffer = null;
	}
	
	public int read(long fileOffset, byte []destination,
		int destinationOffset, int length) throws IOException
	{
		if (_writeBuffer != null)
			_writeBuffer.close();
		if (_readBuffer == null)
			_readBuffer = new ReadableBuffer(_leaser, _readResolver);
		
		return _readBuffer.read(fileOffset, destination, 
			destinationOffset, length);
	}
	
	public void write(long fileOffset, byte []source,
		int sourceOffset, int length) throws IOException
	{
		if (_readBuffer != null)
			_readBuffer.close();
		if (_writeBuffer == null)
			_writeBuffer = new WritableBuffer(_leaser, _writeResolver);
		
		_writeBuffer.write(fileOffset, source, sourceOffset, length);
	}
	
	static private class NonReadableReadResolver implements ReadResolver
	{
		@Override
		public int read(long fileOffset, byte[] destination,
				int destinationOffset, int length) throws IOException
		{
			throw new IOException("File is not readable.");
		}
	}
	
	static private class NonWritableWriteResolver implements WriteResolver
	{
		@Override
		public void write(long fileOffset, byte[] source, int sourceOffset,
				int length) throws IOException
		{
			throw new IOException("File is not writable.");
		}
		
		@Override
		public void truncate(long offset) throws IOException
		{
			throw new IOException("File is not writable.");
		}
	}
}