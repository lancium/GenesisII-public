package edu.virginia.vcgr.genii.container.bes.activity.forks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Calendar;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.rfork.AbstractRandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class WorkingDirFileFork extends AbstractRandomByteIOResourceFork
{
	private File getTargetFile() throws IOException
	{
		File ret;
		String relativePath = getForkPath();
		if (!relativePath.startsWith(WorkingDirectoryFork.FORK_BASE_PATH))
			throw new FileNotFoundException(String.format(
				"Invalid fork path specified (%s).", relativePath));
		relativePath = relativePath.substring(
			WorkingDirectoryFork.FORK_BASE_PATH.length());
		
		if (relativePath.length() > 0 && relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);
		
		IBESActivityResource resource = 
			(IBESActivityResource)getService().getResourceKey().dereference();
		
		BESActivity activity = resource.findActivity();
		BESWorkingDirectory workingDir = activity.getActivityCWD();
		if (relativePath.length() == 0)
			ret = workingDir.getWorkingDirectory();
		else
			ret = new File(workingDir.getWorkingDirectory(), relativePath);
		
		if (!ret.exists())
			throw new FileNotFoundException(String.format(
				"Couldn't find path \"%s\".", getForkPath()));
		if (!ret.isFile())
			throw new IOException(String.format(
				"Target \"%s\" is not a file.", getForkPath()));
		
		return ret;
	}
	
	public WorkingDirFileFork(ResourceForkService service, String forkPath)
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
			raf = new RandomAccessFile(getTargetFile(), "r");
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
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(getTargetFile(), "rw");
			raf.setLength(offset);
			raf.seek(offset);
			raf.getChannel().write(source);
		}
		finally
		{
			StreamUtils.close(raf);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void write(long offset, ByteBuffer source) throws IOException
	{
		RandomAccessFile raf = null;
		
		try
		{
			raf = new RandomAccessFile(getTargetFile(), "rw");
			raf.seek(offset);
			raf.getChannel().write(source);
		}
		finally
		{
			StreamUtils.close(raf);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar accessTime()
	{
		Calendar c = Calendar.getInstance();
		return c;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void accessTime(Calendar newTime)
	{
		// Do nothing
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar createTime()
	{

		Calendar c = Calendar.getInstance();
		return c;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Calendar modificationTime()
	{
		Calendar c = Calendar.getInstance();
		try
		{
			File file = getTargetFile();
			c.setTimeInMillis(file.lastModified());
		}
		catch (IOException cause)
		{
			return c;
		}
		
		return c;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modificationTime(Calendar newTime)
	{
		// Do nothing
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public boolean readable()
	{
		try
		{
			return getTargetFile().canRead();
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
			return getTargetFile().length();
		}
		catch (IOException ioe)
		{
			return 0L;
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public boolean writable()
	{
		try
		{
			return getTargetFile().canWrite();
		}
		catch (IOException ioe)
		{
			return false;
		}
	}
}