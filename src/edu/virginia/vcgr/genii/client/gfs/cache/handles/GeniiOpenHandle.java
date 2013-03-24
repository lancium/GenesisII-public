package edu.virginia.vcgr.genii.client.gfs.cache.handles;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;

public abstract class GeniiOpenHandle<CacheObject>
{
	CacheObject _cacheObject = null;
	String[] _path = null;

	/* Requested deposition */
	public final static int SUPERSEDE = 0; // Delete + Create
	public final static int OPEN = 1;
	public final static int CREATE = 2;
	public final static int OPEN_IF = 3;
	public final static int OVERWRITE = 4; // Truncate
	public final static int OVERWRITE_IF = 5;

	/* Desired Access */
	public final static int INFORMATION_ONLY = 0;
	public final static int FILE_READ_DATA = 1;
	public final static int FILE_WRITE_DATA = 2;
	public final static int FILE_APPEND_DATA = 4;
	public final static int FILE_EXECUTE = 8; // Don't handle
	public final static int DELETE = 16; // Don't handle

	public abstract FilesystemStatStructure stat() throws FSException;

	public abstract boolean isDirectory();

	public String[] getPath()
	{
		return _path;
	}

	public void invalidate()
	{
		_cacheObject = null;
	}
}
