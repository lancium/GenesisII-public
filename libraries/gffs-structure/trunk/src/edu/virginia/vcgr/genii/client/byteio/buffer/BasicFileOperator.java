package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

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

	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver, WriteResolver writeResolver,
		AppendResolver appendResolver, boolean truncate) throws IOException
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

	public BasicFileOperator(ByteIOBufferLeaser leaser, ReadResolver readResolver) throws IOException
	{
		this(leaser, readResolver, null, null, false);
	}

	public BasicFileOperator(ByteIOBufferLeaser leaser, WriteResolver writeResolver, AppendResolver appendResolver,
		boolean truncate) throws IOException
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

	public void read(long fileOffset, ByteBuffer destination) throws IOException
	{
		if (_writeBuffer != null) {
			StreamUtils.close(_writeBuffer);
			_writeBuffer = null;
		}

		if (_appendBuffer != null) {
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}

		if (_readBuffer == null)
			_readBuffer = new ReadableBuffer(_leaser, _readResolver);

		_readBuffer.read(fileOffset, destination);
	}

	public void write(long fileOffset, ByteBuffer source) throws IOException
	{
		if (_readBuffer != null) {
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}

		if (_appendBuffer != null) {
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}

		if (_writeBuffer == null)
			_writeBuffer = new WritableBuffer(_leaser, _writeResolver);

		_writeBuffer.write(fileOffset, source);
	}

	public void truncate(long fileOffset) throws IOException
	{
		if (_readBuffer != null) {
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}

		if (_appendBuffer != null) {
			StreamUtils.close(_appendBuffer);
			_appendBuffer = null;
		}

		if (_writeBuffer == null)
			_writeBuffer = new WritableBuffer(_leaser, _writeResolver);

		_writeBuffer.truncate(fileOffset);
	}

	public void append(ByteBuffer source) throws IOException
	{
		if (_readBuffer != null) {
			StreamUtils.close(_readBuffer);
			_readBuffer = null;
		}

		if (_writeBuffer != null) {
			StreamUtils.close(_writeBuffer);
			_writeBuffer = null;
		}

		if (_appendBuffer == null)
			_appendBuffer = new AppendableBuffer(_leaser, _appendResolver);

		_appendBuffer.append(source);
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
		public void read(long fileOffset, ByteBuffer destination) throws IOException
		{
			throw new IOException("File is not readable.");
		}
	}

	static private class NonWritableWriteResolver implements WriteResolver
	{
		@Override
		public void truncate(long offset) throws IOException
		{
			throw new IOException("File is not writable.");
		}

		@Override
		public void write(long fileOffset, ByteBuffer source) throws IOException
		{
			throw new IOException("File is not writable.");
		}
	}

	static private class NonWritableAppendResolver implements AppendResolver
	{
		@Override
		public void append(ByteBuffer source) throws IOException
		{
			throw new IOException("File is not writable.");
		}
	}
}