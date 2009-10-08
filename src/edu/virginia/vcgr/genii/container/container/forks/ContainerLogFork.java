package edu.virginia.vcgr.genii.container.container.forks;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Calendar;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.rfork.AbstractRandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class ContainerLogFork extends AbstractRandomByteIOResourceFork
{
	static private File getContainerLogFile()
	{
		return new File(Installation.getInstallDirectory(), "container.log");
	}
	
	public ContainerLogFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void read(long offset, ByteBuffer dest) throws IOException
	{
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(getContainerLogFile(), "r");
			raf.seek(offset);
			raf.getChannel().read(dest);
		}
		finally
		{
			StreamUtils.close(raf);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("TruncAppend not permitted on container.log");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void write(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("Write not permitted on container.log");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar accessTime()
	{
		return Calendar.getInstance();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void accessTime(Calendar newTime)
	{
		// Ignore
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar createTime()
	{
		return Calendar.getInstance();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar modificationTime()
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getContainerLogFile().lastModified());
		return c;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modificationTime(Calendar newTime)
	{
		// Ignore
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public boolean readable()
	{
		return true;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public long size()
	{
		return getContainerLogFile().length();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public boolean writable()
	{
		return false;
	}
}