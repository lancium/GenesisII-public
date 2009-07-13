package edu.virginia.vcgr.genii.container.iterator;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IteratorContentsInputFile implements Closeable
{
	private RandomAccessFile _raf = null;
	
	private OffsetLengthPair []_table = null;
	
	private void readFully(FileChannel channel, ByteBuffer dest)
		throws IOException
	{
		while (dest.hasRemaining())
		{
			if (channel.read(dest) <= 0)
				throw new IOException("Unable to read file.");
		}
	}
	
	private void readTable() throws IOException
	{
		ByteBuffer oneLong = ByteBuffer.allocate(8);
		FileChannel channel = _raf.getChannel();
		_raf.seek(_raf.length() - 8);
		readFully(channel, oneLong);
		oneLong.flip();
		long startOfTable = oneLong.getLong();
		_raf.seek(startOfTable);
		ByteBuffer tableBuffer = ByteBuffer.allocate(
			(int)(_raf.length() - startOfTable - 16));
		readFully(channel, tableBuffer);
		tableBuffer.flip();
		_table = new OffsetLengthPair[tableBuffer.remaining() / 16];
		for (int lcv = 0; lcv < _table.length; lcv++)
			_table[lcv] = new OffsetLengthPair(
				tableBuffer.getLong(), tableBuffer.getLong());
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	public IteratorContentsInputFile(File source) throws IOException
	{
		_raf = new RandomAccessFile(source, "r");
		readTable();
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_raf != null)
			_raf.close();
	}
	
	final public int size()
	{
		return _table.length;
	}
	
	public InputStream openEntry(int entryNumber) throws IOException
	{
		OffsetLengthPair pair = _table[entryNumber];
		return new BoundedInputStream(pair.offset(), pair.length());
	}
	
	private class BoundedInputStream extends InputStream
	{
		private long _remaining;
		
		private BoundedInputStream(long startOffset, long length)
			throws IOException
		{
			_raf.seek(startOffset);
			_remaining = length;
		}

		@Override
		public int available() throws IOException
		{
			return (int)_remaining;
		}

		@Override
		public int read() throws IOException
		{
			if (_remaining <= 0)
				return -1;
			int ret = _raf.read();
			_remaining--;
			return ret;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			if (len > _remaining)
				len = (int)_remaining;
			
			int read = _raf.read(b, off, len);
			_remaining -= len;
			return read;
		}

		@Override
		public int read(byte[] b) throws IOException
		{
			return read(b, 0, b.length);
		}

		@Override
		public long skip(long n) throws IOException
		{
			return (long)read(new byte[(int)n]);
		}
	}
}