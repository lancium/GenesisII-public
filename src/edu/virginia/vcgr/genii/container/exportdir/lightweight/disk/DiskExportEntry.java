package edu.virginia.vcgr.genii.container.exportdir.lightweight.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.fslock.FSLock;
import edu.virginia.vcgr.genii.client.io.fslock.FSLockManager;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportDir;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportFile;

public class DiskExportEntry extends AbstractVExportEntry
	implements VExportDir, VExportFile
{
	static private FSLockManager _lockManager = new FSLockManager();
	
	private File _target;
	
	public DiskExportEntry(File target) throws IOException
	{
		super(target.getName(), target.isDirectory());

		_target = target;
		
		if (!_target.exists())
			throw new FileNotFoundException(String.format(
				"Unable to locate file system entry \"%s\".",
				_target));
	}

	@Override
	public boolean createFile(String newFileName) throws IOException
	{
		return new File(_target, newFileName).createNewFile();
	}

	@Override
	public Collection<VExportEntry> list(String name) throws IOException
	{
		Collection<VExportEntry> entries = new LinkedList<VExportEntry>();
		
		for (File entry : _target.listFiles())
		{
			if (name == null || name.equals(entry.getName()))
				entries.add(new DiskExportEntry(entry));
		}
		
		return entries;
	}

	@Override
	public boolean mkdir(String newDirName) throws IOException
	{
		return new File(_target, newDirName).mkdir();
	}

	@Override
	public boolean remove(String entryName) throws IOException
	{
		return new File(_target, entryName).delete();
	}

	@Override
	public Calendar accessTime() throws IOException
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar c) throws IOException
	{
		// Do nothing
	}

	@Override
	public Calendar createTime() throws IOException
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(_target.lastModified());
		return c;
	}

	@Override
	public void modificationTime(Calendar c) throws IOException
	{
		// Do nothing
	}

	@Override
	public void read(long offset, ByteBuffer target) throws IOException
	{
		RandomAccessFile raf = null;
		FSLock lock = null;
		
		try
		{
			lock = _lockManager.acquire(_target);
			
			raf = new RandomAccessFile(_target, "r");
			raf.seek(offset);
			raf.getChannel().read(target);
		}
		finally
		{
			StreamUtils.close(raf);
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public boolean readable() throws IOException
	{
		return _target.canRead();
	}

	@Override
	public long size() throws IOException
	{
		FSLock lock = null;
		
		try
		{
			lock = _lockManager.acquire(_target);
			return _target.length();
		}
		finally
		{
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		FSLock lock = null;
		RandomAccessFile raf = null;
		
		try
		{
			lock = _lockManager.acquire(_target);
			
			raf = new RandomAccessFile(_target, "rw");
			raf.setLength(offset);
			raf.seek(offset);
			raf.getChannel().write(source);
		}
		finally
		{
			StreamUtils.close(raf);
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public boolean writable() throws IOException
	{
		return _target.canWrite();
	}

	@Override
	public void write(long offset, ByteBuffer source) throws IOException
	{
		FSLock lock = null;
		RandomAccessFile raf = null;
		
		try
		{
			lock = _lockManager.acquire(_target);
			raf = new RandomAccessFile(_target, "rw");
			raf.seek(offset);
			raf.getChannel().write(source);
		}
		finally
		{
			StreamUtils.close(raf);
			if (lock != null)
				lock.release();
		}
	}
}