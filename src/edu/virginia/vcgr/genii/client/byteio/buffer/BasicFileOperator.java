package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;

import org.morgan.util.io.StreamUtils;

public class BasicFileOperator implements Closeable
{
	private ReadResolver _readResolver;
	private WriteResolver _writeResolver;
	private AppendResolver _appendResolver;
	
	private ByteIOBufferLeaser _leaser;
	
	private ReadableBuffer _readBuffer = null;
	private WritableBuffer _writeBuffer = null;
	private AppendableBuffer _appendBuffer = null;
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver,
		WriteResolver writeResolver, AppendResolver appendResolver,
		boolean truncate) throws IOException
	{
		_leaser = leaser;
		
		if (readResolver == null)
			readResolver = new NonReadableReadResolver();
		
		if (writeResolver == null)
			writeResolver = new NonWritableWriteResolver();
		
		if (appendResolver == null)
			appendResolver = new NonWritableAppendResolver();
		
		_readResolver = readResolver;
		_writeResolver = writeResolver;
		_appendResolver = appendResolver;
		
		if (truncate)
			_writeResolver.truncate(0L);
	}
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver)
		throws IOException
	{
		this(leaser, readResolver, null, null, false);
	}
	
	public BasicFileOperator(ByteIOBufferLeaser leaser, WriteResolver writeResolver,
		AppendResolver appendResolver, boolean truncate) throws IOException
	{
		this(leaser, null, writeResolver, appendResolver, truncate);
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
		if (_appendBuffer != null)
			_appendBuffer.close();
		
		_readBuffer = null;
		_writeBuffer = null;
		_appendBuffer = null;
	}
	
	public int read(long fileOffset, byte []destination,
		int destinationOffset, int length) throws IOException
	{
		if (_writeBuffer != null)
		{
			StreamUtils.close(_writeBuffer);
			_writeBuffer = null;
		}
		
		if (_appendBuffer != null)
		{
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}
		
		if (_readBuffer == null)
			_readBuffer = new ReadableBuffer(_leaser, _readResolver);
		
		return _readBuffer.read(fileOffset, destination, 
			destinationOffset, length);
	}
	
	public void write(long fileOffset, byte []source,
		int sourceOffset, int length) throws IOException
	{
		if (_readBuffer != null)
		{
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}
		
		if (_appendBuffer != null)
		{
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}
		
		if (_writeBuffer == null)
			_writeBuffer = new WritableBuffer(_leaser, _writeResolver);
		
		_writeBuffer.write(fileOffset, source, sourceOffset, length);
	}
	
	public void truncate(long fileOffset) throws IOException
	{
		if (_readBuffer != null)
		{
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}
		
		if (_appendBuffer != null)
		{
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}
		
		if (_writeBuffer == null)
			_writeBuffer = new WritableBuffer(_leaser, _writeResolver);
		
		_writeBuffer.truncate(fileOffset);
	}
	
	public void append(byte []source, int start, int length)
		throws IOException 
	{
		if (_readBuffer != null)
		{
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}
		
		if (_writeBuffer != null)
		{
			StreamUtils.close(_writeBuffer);
			_writeBuffer = null;
		}
		
		if (_appendBuffer == null)
			_appendBuffer = new AppendableBuffer(_leaser, _appendResolver);
		
		_appendBuffer.append(source, start, length);
	}
	
	public void flush() throws IOException
	{
		if (_writeBuffer != null)
			_writeBuffer.flush();
		if (_appendBuffer != null)
			_appendBuffer.flush();
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
	
	static private class NonWritableAppendResolver implements AppendResolver
	{
		@Override
		public void append(byte[] data, int start, int length)
				throws IOException
		{
			throw new IOException("File is not writable.");
		}
	}
}