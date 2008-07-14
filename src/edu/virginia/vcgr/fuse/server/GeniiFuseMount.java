package edu.virginia.vcgr.fuse.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fuse.exceptions.FuseBadFileHandleException;
import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.exceptions.FuseFunctionNotImplementedException;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.fuse.fs.FuseFileSystem;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import fuse.Filesystem;
import fuse.FilesystemConstants;
import fuse.FuseDirEnt;
import fuse.FuseException;
import fuse.FuseStat;
import fuse.FuseStatfs;

public class GeniiFuseMount implements Filesystem
{
	static private Log _logger = LogFactory.getLog(GeniiFuseMount.class);
	
	private FuseFileSystem _fs;
	private int _uid;
	private long _nextHandle = 0;
	
	private Map<Long, FuseFile> _openFiles = new HashMap<Long, FuseFile>();
	
	private FuseFile findFile(long fileHandle) throws FuseException
	{
		FuseFile ff = _openFiles.get(new Long(fileHandle));
		if (ff == null)
			throw new FuseBadFileHandleException("Unknown filehandle given.");
		
		return ff;
	}
	
	static private int getMode(FuseFileSystemEntry entry) throws FuseException
	{
		int mode = 0x0;
		
		try
		{
			mode = entry.getPermissions();
		}
		catch (Throwable cause)
		{
			_logger.debug("An exceptino occurred trying to get permissions.",
				cause);
			mode = 0x0;
		}
		
		if (entry.isDirectory())
			mode |= FuseStat.TYPE_DIR;
		else if (entry.isSymlink())
			mode |= FuseStat.TYPE_SYMLINK;
		else
			mode |= FuseStat.TYPE_FILE;
		
		return mode;
	}
	
	GeniiFuseMount(FuseFileSystem fs, int uid)
	{
		_fs = fs;
		_uid = uid;
	}
	
	@Override
	public void chmod(String path, int mode) throws FuseException
	{
		_logger.trace(String.format("chmod(%s, 0%o)", path, mode));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		entry.setPermissions(mode);
	}

	@Override
	public void chown(String path, int uid, int gid) throws FuseException
	{
		_logger.trace(String.format("chmod(%s, %d, %d)", path, uid, gid));
		
		throw new FuseFunctionNotImplementedException("Cannot chown files.");
	}

	@Override
	public void flush(String path, long fileHandle) throws FuseException
	{
		_logger.trace(String.format("flush(%s, %d)", path, fileHandle));
		
		FuseFile ff = findFile(fileHandle);
		ff.flush();
	}

	@Override
	public void fsync(String path, long fileHandle, boolean isDatasync)
			throws FuseException
	{
		_logger.trace(String.format("fsync(%s, %d, %s)", path, fileHandle, isDatasync));
		
		// For now, we don't do this.
	}

	@Override
	public FuseStat getattr(String path) throws FuseException
	{
		_logger.trace(String.format("getattr(%s)", path));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		
		FuseStat stat = new FuseStat();
		
		stat.atime = (int)(entry.accessTime().getTimeInMillis() / 1000L);
		stat.ctime = (int)(entry.createTime().getTimeInMillis() / 1000L);
		stat.mtime = (int)(entry.modificationTime().getTimeInMillis() / 1000L);
		stat.gid = 0;
		stat.uid = _uid;
		stat.mode = getMode(entry);
		stat.nlink = 1;
		stat.size = entry.length();
		stat.blocks = (int)(stat.size + (_fs.blockSize() - 1)) / 
			_fs.blockSize();
		
		return stat;
	}

	@Override
	public FuseDirEnt[] getdir(String path) throws FuseException
	{
		_logger.trace(String.format("getdir(%s)", path));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		Collection<FuseDirEnt> entries = new LinkedList<FuseDirEnt>();
		for (FuseFileSystemEntry subEntry : entry.listContents())
		{
			FuseDirEnt directoryEntry = new FuseDirEnt();
			
			directoryEntry.inode = (int)subEntry.inode();
			directoryEntry.mode = getMode(subEntry);
			directoryEntry.name = subEntry.name();
			
			entries.add(directoryEntry);
		}
		
		return entries.toArray(new FuseDirEnt[0]);
	}

	@Override
	public void link(String from, String to) throws FuseException
	{
		_logger.trace(String.format("link(%s, %s)", from, to));
		
		FuseFileSystemEntry fromEntry = _fs.lookup(from);
		FuseFileSystemEntry toEntry = _fs.lookup(to);
		
		toEntry.link(fromEntry);
	}

	@Override
	public void mkdir(String path, int mode) throws FuseException
	{
		_logger.trace(String.format("mkdir(%s, 0%o)", path, mode));
		
		FuseFileSystemEntry dir = _fs.lookup(path);
		dir.mkdir(mode);
	}

	@Override
	public void mknod(String path, int mode, int rdev) throws FuseException
	{
		_logger.trace(String.format("mknod(%s, 0%o, %d)", path, mode, rdev));
		
		if (rdev == 0)
		{
			FuseFileSystemEntry entry = _fs.lookup(path);
			FuseFile ff = entry.open(
				true, true, true, true, false, null);
			
			try
			{
				ff.close();
			}
			catch (IOException ioe)
			{
				throw FuseExceptions.translate("Couldn't close file.", ioe);
			}
		} else
		{
			throw new FuseFunctionNotImplementedException(
				"Haven't implemented mknod yet.");
		}
	}

	@Override
	public long open(String path, int flags) throws FuseException
	{
		_logger.trace(String.format("open(%s, 0x%x)", path, flags));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		
		boolean readable = ((flags & FilesystemConstants.O_WRONLY) == 0);
		boolean writable = (flags & 
			(FilesystemConstants.O_RDWR | FilesystemConstants.O_WRONLY)) > 0;
		
		FuseFile ff = entry.open(false, false, readable, writable, false, null);
		long handle = _nextHandle++;
		_openFiles.put(new Long(handle), ff);
		return handle;
	}

	@Override
	public void read(String path, long fileHandle, ByteBuffer buffer, long offset)
			throws FuseException
	{
		_logger.trace(String.format("read(%s, %d, %s, %d)", path, fileHandle, 
			buffer, offset));
		
		FuseFile ff = findFile(fileHandle);
		ff.read(offset, buffer);
	}

	@Override
	public String readlink(String path) throws FuseException
	{
		_logger.trace(String.format("readlink(%s)", path));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		return entry.readlink().pwd();
	}

	@Override
	public void release(String path, long fileHandle, int flags) throws FuseException
	{
		_logger.trace(String.format("release(%s, %d, 0x%x)", path, fileHandle, flags));
		
		FuseFile ff = findFile(fileHandle);
		try
		{
			ff.close();
			ff.release();
		}
		catch (IOException ioe)
		{
			throw FuseExceptions.translate("Couldn't close file.", ioe);
		}
		
		_openFiles.remove(fileHandle);
	}

	@Override
	public void rename(String from, String to) throws FuseException
	{
		_logger.trace(String.format("rename(%s, %s)", from, to));
		
		FuseFileSystemEntry fromEntry = _fs.lookup(from);
		FuseFileSystemEntry toEntry = _fs.lookup(to);
		
		toEntry.rename(fromEntry);
	}

	@Override
	public void rmdir(String path) throws FuseException
	{
		_logger.trace(String.format("rmdir(%s)", path));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		entry.delete(false);
	}

	@Override
	public FuseStatfs statfs() throws FuseException
	{
		_logger.trace("statfs()");
		
		FuseStatfs ret = new FuseStatfs();
		ret.blocks = _fs.totalBlocks();
		ret.blocksFree = _fs.blocksFree();
		ret.blockSize = _fs.blockSize();
		ret.files = _fs.totalFiles();
		ret.filesFree = _fs.filesFree();
		ret.namelen = _fs.maxEntryNameLength();
		
		return ret;
	}

	@Override
	public void symlink(String from, String to) throws FuseException
	{
		_logger.trace(String.format("symlink(%s, %s)", from, to));
		
		FuseFileSystemEntry fromEntry = _fs.lookup(from);
		FuseFileSystemEntry toEntry = _fs.lookup(to);
		
		toEntry.symlink(fromEntry);
	}

	@Override
	public void truncate(String path, long newSize) throws FuseException
	{
		_logger.trace(String.format("truncate(%s, %d)", path, newSize));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		FuseFile ff = entry.open(false, false, false, true, false, new Long(newSize));
		try
		{
			ff.close();
		}
		catch (IOException ioe)
		{
			throw FuseExceptions.translate("Couldn't close file.", ioe);
		}
	}

	@Override
	public void unlink(String path) throws FuseException
	{
		_logger.trace(String.format("unlink(%s)", path));
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		entry.delete(false);
	}

	@Override
	public void utime(String path, int atime, int mtime)
		throws FuseException
	{
		_logger.trace(String.format("utime(%s, %d, %d)", path, atime, mtime));
		
		Calendar aTime = Calendar.getInstance();
		aTime.setTimeInMillis(atime * 1000L);
		
		Calendar mTime = Calendar.getInstance();
		mTime.setTimeInMillis(mtime * 1000L);
		
		FuseFileSystemEntry entry = _fs.lookup(path);
		entry.accessTime(aTime);
		entry.modificationTime(mTime);
	}

	@Override
	public void write(String path, long fileHandle, boolean isWritepage, 
		ByteBuffer buffer, long offset) throws FuseException
	{
		_logger.trace(String.format("write(%s, %d, %s, %s, %d)",
			path, fileHandle, isWritepage, buffer, offset));
		
		FuseFile ff = findFile(fileHandle);
		ff.write(offset, buffer);
	}
}