package edu.virginia.vcgr.genii.client.gfs.cache.objects;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheManager;

/**
 * This abstract class is responsible for caching all information obtained for a resource
 */
public abstract class GeniiCachedResource
{
	protected boolean invalidated = false;
	protected long timeOfEntry;
	FilesystemStatStructure _statStructure;

	String[] _path = null;
	GenesisIIFilesystem _fs = null;

	public GeniiCachedResource(String[] path)
	{
		timeOfEntry = System.currentTimeMillis();
		_fs = GeniiCacheManager.getInstance().get_fs();
		_path = path;
	}

	public synchronized FilesystemStatStructure stat() throws FSException
	{
		if (_statStructure == null) {
			_statStructure = _fs.stat(_path);
		}
		return _statStructure;
	}

	public void setPath(String[] path)
	{
		_path = path;
	}

	public abstract void rename(String[] toPath) throws FSException;

	/**
	 * Refreshes the cache
	 */
	public abstract void refresh() throws FSException;

	public abstract boolean isDirectory();

	public boolean isValid()
	{
		return !invalidated;
	}

	public abstract void invalidate();

	public long getTimeOfEntry()
	{
		return timeOfEntry;
	}

	public void setTimeOfEntry(long timeOfEntry)
	{
		this.timeOfEntry = timeOfEntry;
	}

	public synchronized void updateTimes(long accessTime, long modificationTime) throws FSException
	{
		_statStructure = new FilesystemStatStructure(_statStructure.getINode(), _statStructure.getName(),
			_statStructure.getEntryType(), _statStructure.getSize(), _statStructure.getCreated(), modificationTime, accessTime,
			_statStructure.getPermissions());
		_fs.updateTimes(_path, accessTime, modificationTime);
	}
}