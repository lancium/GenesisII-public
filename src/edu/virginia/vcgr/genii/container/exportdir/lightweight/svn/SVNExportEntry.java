package edu.virginia.vcgr.genii.container.exportdir.lightweight.svn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportDir;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportFile;

class SVNExportEntry extends AbstractVExportEntry 
	implements VExportFile, VExportDir
{
	private SVNExportEntryCache _entryCache;
	
	private String _svnURL;
	private String _relativePath;
	private SVNRepository _repository;
	private long _revision;
	private SVNDirEntry _entry;
	
	private String getRelativePath()
	{
		return _relativePath;
	}
	
	SVNExportEntry(SVNRepository repository, String svnURL, long revision,
		SVNDirEntry entry, String relativePath, SVNExportEntryCache entryCache)
	{
		super(entry.getName(), entry.getKind() == SVNNodeKind.DIR);
		
		_svnURL = svnURL;
		_repository = repository;
		_revision = revision;
		_entry = entry;
		_relativePath = relativePath;
		
		_entryCache = entryCache;
	}
	
	@Override
	public Calendar accessTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		c.setTime(_entry.getDate());
		return c;
	}

	@Override
	public void accessTime(Calendar c) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@Override
	public Calendar createTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0L);
		return c;
	}

	@Override
	public Calendar modificationTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		c.setTime(_entry.getDate());
		return c;
	}

	@Override
	public void modificationTime(Calendar c) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@Override
	public void read(long offset, ByteBuffer target) throws IOException
	{
		PartialBlockOutputStream out = new PartialBlockOutputStream(
			target, offset);
		
		try
		{
			_repository.getFile(getRelativePath(), _revision, null, out);
		}
		catch (SVNException e)
		{
			throw new IOException("Unable to read SVN file.", e);
		}
	}

	@Override
	public boolean readable() throws IOException
	{
		return true;
	}

	@Override
	public long size() throws IOException
	{
		return _entry.getSize();
	}

	@Override
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@Override
	public boolean writable() throws IOException
	{
		return false;
	}

	@Override
	public void write(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@Override
	public boolean createFile(String newFileName) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<VExportEntry> list(String name) throws IOException
	{
		Collection<VExportEntry> ret = new LinkedList<VExportEntry>();
		
		try
		{
			Collection<SVNDirEntry> contents = _repository.getDir(
				getRelativePath(), _revision, (SVNProperties)null, 
				(Collection<?>)null);
			for (SVNDirEntry entry : contents)
			{
				String myPath = getRelativePath();
				if (name == null || name.equals(entry.getName()))
					ret.add(_entryCache.lookup(new SVNPathIdentifier(
						_repository, _svnURL, 
						(myPath.length() == 0) ? entry.getName() :
							String.format("%s/%s",
								getRelativePath(), entry.getName()),
						_revision)));
			}
			
			return ret;
		}
		catch (SVNException e)
		{
			throw new IOException(
				"Unable to list contents of directory.", e);
		}
	}

	@Override
	public boolean mkdir(String newDirName) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}

	@Override
	public boolean remove(String entryName) throws IOException
	{
		throw new IOException("SVN Exports are read-only.");
	}
}