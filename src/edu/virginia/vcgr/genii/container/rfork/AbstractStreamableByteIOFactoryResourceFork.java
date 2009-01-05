package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public abstract class AbstractStreamableByteIOFactoryResourceFork 
	extends AbstractResourceFork implements StreamableByteIOFactoryResourceFork
{
	static private class SimpleCountingStream extends OutputStream
	{
		private long _count = 0;
		
		public long getCount()
		{
			return _count;
		}
		
		@Override
		public void write(byte[] b)
		{
			_count += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len)
		{
			_count += len;
		}
		
		@Override
		public void write(int b) throws IOException
		{
			_count++;
		}	
	}
	
	protected AbstractStreamableByteIOFactoryResourceFork(
			ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	public Calendar accessTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar newTime)
	{
		// do nothing
	}

	@Override
	public Calendar createTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public void modificationTime(Calendar newTime)
	{
		// do nothing
	}

	@Override
	public boolean readable()
	{
		return true;
	}

	@Override
	public boolean writable()
	{
		return true;
	}
	
	@Override
	public long size()
	{
		try
		{
			SimpleCountingStream stream = new SimpleCountingStream();
			snapshotState(stream);
			return stream.getCount();
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Unable to get size of stream.", ioe);
		}
	}
}