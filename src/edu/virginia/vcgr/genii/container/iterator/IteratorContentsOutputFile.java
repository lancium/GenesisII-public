package edu.virginia.vcgr.genii.container.iterator;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class IteratorContentsOutputFile implements Closeable
{
	static final private int BUFFER_SIZE = 16 * 1024;
	
	private FileOutputStream _out = null;
	private long _currentOffset = 0L;
	private CountingOutputStream _currentEntry = null;
	
	private List<ByteBuffer> _tableBuffers = new LinkedList<ByteBuffer>();
	private ByteBuffer _currentBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	
	private void addLongs(long first, long second)
	{
		if (_currentBuffer.remaining() < 16)
		{
			_currentBuffer.flip();
			_tableBuffers.add(_currentBuffer);
			_currentBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		}
		
		_currentBuffer.putLong(first);
		_currentBuffer.putLong(second);
	}
	
	private void writeTable() throws IOException
	{
		addLongs(-1L, _currentOffset);
		_currentBuffer.flip();
		_tableBuffers.add(_currentBuffer);
		FileChannel channel = _out.getChannel();
		channel.write(_tableBuffers.toArray(
			new ByteBuffer[_tableBuffers.size()]));
		channel.close();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	public IteratorContentsOutputFile(File target) throws IOException
	{
		_out = new FileOutputStream(target);
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_out != null)
		{
			if (_currentEntry != null)
				_currentEntry.close();
			
			writeTable();
			_out.close();
		}
	}
	
	synchronized public OutputStream addEntry()
	{
		if (_currentEntry != null)
			_currentEntry.close();
		return (_currentEntry = new CountingOutputStream());
	}
	
	private class CountingOutputStream extends OutputStream
	{
		private long _startOffset = 0L;
		
		private CountingOutputStream()
		{
			_startOffset = _currentOffset;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			_out.write(b);
			_currentOffset++;
		}

		@Override
		synchronized public void close()
		{
			if (_currentEntry != null)
			{
				addLongs(_startOffset, _currentOffset - _startOffset);
				_currentEntry = null;
			}
		}

		@Override
		public void flush() throws IOException
		{
			_out.flush();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			_out.write(b, off, len);
			_currentOffset += len;
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			_out.write(b);
			_currentOffset += b.length;
		}
	}
}