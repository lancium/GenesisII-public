package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.rfork.AbstractRandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.RandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class LightWeightExportFileFork extends AbstractRandomByteIOResourceFork
		implements RandomByteIOResourceFork
{
	final private VExportFile getTarget() throws IOException
	{
		return LightWeightExportUtils.getFile(getForkPath());
	}
	
	public LightWeightExportFileFork(ResourceForkService service,
			String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void read(long offset, ByteBuffer dest) throws IOException
	{
		getTarget().read(offset, dest);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		getTarget().truncAppend(offset, source);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void write(long offset, ByteBuffer source) throws IOException
	{
		getTarget().write(offset, source);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar accessTime()
	{
		try
		{
			return getTarget().accessTime();
		}
		catch (IOException ioe)
		{
			return Calendar.getInstance();
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void accessTime(Calendar newTime)
	{
		try
		{
			getTarget().accessTime(newTime);
		}
		catch (IOException ioe)
		{
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar createTime()
	{
		try
		{
			return getTarget().createTime();
		}
		catch (IOException ioe)
		{
			return Calendar.getInstance();
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Calendar modificationTime()
	{
		try
		{
			return getTarget().modificationTime();
		}
		catch (IOException ioe)
		{
			return Calendar.getInstance();
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void modificationTime(Calendar newTime)
	{
		try
		{
			getTarget().modificationTime(newTime);
		}
		catch (IOException ioe)
		{
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public boolean readable()
	{
		try
		{
			return getTarget().readable();
		}
		catch (IOException ioe)
		{
			return false;
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public long size()
	{
		try
		{
			return getTarget().size();
		}
		catch (IOException ioe)
		{
			return 0L;
		}
	}

	@Override
	public boolean writable()
	{
		try
		{
			return getTarget().writable();
		}
		catch (IOException ioe)
		{
			return false;
		}
	}
}