package edu.virginia.vcgr.genii.client.fuse;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.path.FilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.security.PermissionBits;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.genii.client.fuse.exceptions.FuseFunctionNotImplementedException;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import fuse.Filesystem;
import fuse.FilesystemConstants;
import fuse.FuseDirEnt;
import fuse.FuseException;
import fuse.FuseStat;
import fuse.FuseStatfs;

public class GeniiFuseMount implements Filesystem
{
	static private Log _logger = LogFactory.getLog(GeniiFuseMount.class);
	
	static private final int BLOCK_SIZE = 512;
	static private final int FILENAME_SIZE = 1024;
	
	static private FilesystemPathRepresentation PATHREP =
		UnixFilesystemPathRepresentation.INSTANCE;
	
	static private Permissions permissionsFromMode(int mode)
	{
		Permissions p = new Permissions();
		
		p.set(PermissionBits.OWNER_READ,
				(mode & FuseStat.OWNER_READ) > 0);
		p.set(PermissionBits.OWNER_WRITE,
				(mode & FuseStat.OWNER_WRITE) > 0);
		p.set(PermissionBits.OWNER_EXECUTE, 
			(mode & FuseStat.OWNER_EXECUTE) > 0);
		
		p.set(PermissionBits.GROUP_READ,
				(mode & FuseStat.GROUP_READ) > 0);
		p.set(PermissionBits.GROUP_WRITE,
				(mode & FuseStat.GROUP_WRITE) > 0);
		p.set(PermissionBits.GROUP_EXECUTE, 
			(mode & FuseStat.GROUP_EXECUTE) > 0);
		
		p.set(PermissionBits.EVERYONE_READ,
				(mode & FuseStat.OTHER_READ) > 0);
		p.set(PermissionBits.EVERYONE_WRITE, 
			(mode & FuseStat.OTHER_WRITE) > 0);
		p.set(PermissionBits.EVERYONE_EXECUTE, 
			(mode & FuseStat.OTHER_EXECUTE) > 0);
		
		return p;
	}
	
	static private int getMode(FilesystemStatStructure statstruct)
	{
		int mode = 0x0;
		
		Permissions p = statstruct.getPermissions();
		if (p.isSet(PermissionBits.OWNER_READ))
			mode |= FuseStat.OWNER_READ;
		if (p.isSet(PermissionBits.OWNER_WRITE))
			mode |= FuseStat.OWNER_WRITE;
		if (p.isSet(PermissionBits.OWNER_EXECUTE))
			mode |= FuseStat.OWNER_EXECUTE;
		
		if (p.isSet(PermissionBits.GROUP_READ))
			mode |= FuseStat.GROUP_READ;
		if (p.isSet(PermissionBits.GROUP_WRITE))
			mode |= FuseStat.GROUP_WRITE;
		if (p.isSet(PermissionBits.GROUP_EXECUTE))
			mode |= FuseStat.GROUP_EXECUTE;
		
		if (p.isSet(PermissionBits.EVERYONE_READ))
			mode |= FuseStat.OTHER_READ;
		if (p.isSet(PermissionBits.EVERYONE_WRITE))
			mode |= FuseStat.OTHER_WRITE;
		if (p.isSet(PermissionBits.EVERYONE_EXECUTE))
			mode |= FuseStat.OTHER_EXECUTE;
		
		FilesystemEntryType entryType = statstruct.getEntryType();
		if (entryType == FilesystemEntryType.DIRECTORY)
			mode |= FuseStat.TYPE_DIR;
		else
			mode |= FuseStat.TYPE_FILE;
		
		return mode;
	}
	
	private int _uid;
	private GenesisIIFilesystem _fs;
	
	private Map<String, Long> _mknodFiles = new HashMap<String, Long>();
	
	public GeniiFuseMount(GenesisIIFilesystem fs, int uid)
	{
		_uid = uid;
		_fs = fs;
	}
	
	@Override
	public void chmod(String path, int mode) throws FuseException
	{
		_logger.trace(String.format("chmod(%s, 0%o)", path, mode));
		
		try
		{
			_fs.chmod(PATHREP.parse(null, path),
				permissionsFromMode(mode));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to chmod target.", cause);
		}
	}

	@Override
	public void chown(String path, int uid, int gid) throws FuseException
	{
		_logger.trace(String.format("chown(%s, %d, %d)", path, uid, gid));
		
		throw new FuseFunctionNotImplementedException("Cannot chown entries.");
	}

	@Override
	public void flush(String path, long fileHandle) throws FuseException
	{
		_logger.trace(String.format("flush(%s, %d)", path, fileHandle));
		
		try
		{
			_fs.flush(fileHandle);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to flush target.", cause);
		}
	}

	@Override
	public void fsync(String path, long fileHandle, boolean isDatasync)
			throws FuseException
	{
		_logger.trace(String.format("fsync(%s, %d, %s)", path, fileHandle, 
			isDatasync));
		
		// For now, we don't do this.
	}

	@Override
	public FuseStat getattr(String path) throws FuseException
	{
		_logger.trace(String.format("getattr(%s)", path));
		
		try
		{
			FilesystemStatStructure statstruct = _fs.stat(PATHREP.parse(
				null, path));
			FuseStat ret = new FuseStat();
			
			ret.atime = (int)(statstruct.getLastAccessed() / 1000L);
			ret.ctime = (int)(statstruct.getCreated() / 1000L);
			ret.mtime = (int)(statstruct.getLastModified() / 1000L);
			ret.gid = 0;
			ret.uid = _uid;
			ret.mode = getMode(statstruct);
			ret.nlink = 1;
			ret.size = statstruct.getSize();
			ret.blocks = (int)(ret.size + (BLOCK_SIZE - 1)) / BLOCK_SIZE;
			
			return ret;
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to flush target.", cause);
		}
	}

	@Override
	public FuseDirEnt[] getdir(String path) throws FuseException
	{
		_logger.trace(String.format("getdir(%s)", path));
		DirectoryHandle dHandle = null;
		LinkedList<FuseDirEnt> entries = new LinkedList<FuseDirEnt>();
		
		try
		{
			dHandle = _fs.listDirectory(PATHREP.parse(null, path));
			Iterator<FilesystemStatStructure> iterator = dHandle.iterator();
			while (iterator.hasNext())
			{
				FilesystemStatStructure struct = iterator.next();
				FuseDirEnt entry = new FuseDirEnt();
				entry.inode = struct.getINode();
				entry.mode = getMode(struct);
				entry.name = struct.getName();
				
				entries.add(entry);
			}
			
			return entries.toArray(new FuseDirEnt[0]);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to list directory.", cause);
		}
		finally
		{
			StreamUtils.close(dHandle);
		}
	}

	@Override
	public void link(String from, String to) throws FuseException
	{
		_logger.trace(String.format("link(%s, %s)", from, to));
		
		try
		{
			_fs.link(PATHREP.parse(null, from), PATHREP.parse(null, to));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to create link.", cause);
		}
	}

	@Override
	public void mkdir(String path, int mode) throws FuseException
	{
		_logger.trace(String.format("mkdir(%s, 0%o)", path, mode));
		
		try
		{
			_fs.mkdir(PATHREP.parse(null, path), permissionsFromMode(mode));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to make directory.", cause);
		}
	}

	@Override
	public void mknod(String path, int mode, int rdev) throws FuseException
	{
		_logger.trace(String.format("mknod(%s, 0%o, %d)", path, mode, rdev));
		
		try
		{
			if (rdev == 0)
			{
				long fileHandle = _fs.open(PATHREP.parse(null, path), 
					new OpenFlags(true, false, false, true),
					OpenModes.READ_WRITE, permissionsFromMode(mode));
				
				// We don't close the file now because it's about to be opened
				// and later released.  Instead, we put it into a table of
				// files that have been created but not closed yet.
				_mknodFiles.put(path, fileHandle);
				return;
			}
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to make node.", cause);
		}
		
		throw new FuseFunctionNotImplementedException(
			"Haven't implemented mknod for devices other than 0.");
	}

	@Override
	public long open(String path, int flags) throws FuseException
	{
		_logger.trace(String.format("open(%s, 0x%x)", path, flags));
		
		// First, let's see if we just created this file with a mknod
		// operation.  If so, it's already open.
		Long handle = _mknodFiles.get(path);
		if (handle != null)
		{
			_mknodFiles.remove(path);
			return handle.longValue();
		}
		
		boolean writable = (flags & 
			(FilesystemConstants.O_RDWR | FilesystemConstants.O_WRONLY)) > 0;
		
		try
		{
			return _fs.open(PATHREP.parse(null, path), 
				new OpenFlags(false, false, false, false), 
				writable ? OpenModes.READ_WRITE : OpenModes.READ, 
				null);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to open files.", cause);
		}
	}

	@Override
	public void read(String path, long fileHandle, ByteBuffer buffer, 
		long offset) throws FuseException
	{
		_logger.trace(String.format("read(%s, %d, %s, %d)",
			path, fileHandle, buffer, offset));
		
		try
		{
			_fs.read(fileHandle, offset, buffer);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to read from file.", cause);
		}
	}

	@Override
	public String readlink(String path) throws FuseException
	{
		_logger.trace(String.format("readlink(%s)", path));
		
		throw new FuseFunctionNotImplementedException(
			"The readLink function is not supported.");
	}

	@Override
	public void release(String path, long fileHandle, int flags) 
		throws FuseException
	{
		_logger.trace(String.format("release(%s, %d, 0x%x, args)", 
			path, fileHandle, flags));
		
		try
		{
			_fs.close(fileHandle);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to release file.", cause);
		}
	}

	@Override
	public void rename(String from, String to) throws FuseException
	{
		_logger.trace(String.format("rename(%s, %s)", from, to));
		
		try
		{
			_fs.rename(PATHREP.parse(null, from), PATHREP.parse(null, to));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to rename.", cause);
		}
	}

	@Override
	public void rmdir(String path) throws FuseException
	{
		_logger.trace(String.format("rmdir(%s)", path));
		
		try
		{
			_fs.unlink(PATHREP.parse(null, path));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to remove directory.", 
				cause);
		}
	}

	@Override
	public FuseStatfs statfs() throws FuseException
	{
		_logger.trace("statfs()");
		
		FuseStatfs ret = new FuseStatfs();
		ret.blocks = Integer.MAX_VALUE;
		ret.blocksFree = Integer.MAX_VALUE;
		ret.blockSize = BLOCK_SIZE;
		ret.files = Integer.MAX_VALUE;
		ret.filesFree = Integer.MAX_VALUE;
		ret.namelen = FILENAME_SIZE;
		
		return ret;
	}

	@Override
	public void symlink(String from, String to) throws FuseException
	{
		_logger.trace(String.format("symlink(%s, %s)", from, to));
		
		throw new FuseFunctionNotImplementedException(
			"symlink is not implemented.");
	}

	@Override
	public void truncate(String path, long newSize) throws FuseException
	{
		_logger.trace(String.format("truncate(%s, %d)", path, newSize));
		
		try
		{
			_fs.truncate(PATHREP.parse(null, path), newSize);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to truncate file.", 
				cause);
		}
	}

	@Override
	public void unlink(String path) throws FuseException
	{
		_logger.trace(String.format("unlink(%s)", path));
		
		try
		{
			_fs.unlink(PATHREP.parse(null, path));
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to unlink file.", 
				cause);
		}
	}

	@Override
	public void utime(String path, int atime, int mtime) throws FuseException
	{
		_logger.trace(String.format("utime(%s, %d, %d)", path, atime, mtime));
		
		try
		{
			_fs.updateTimes(PATHREP.parse(null, path),
				atime * 1000L, mtime * 1000L);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to update times.", 
				cause);
		}
	}

	@Override
	public void write(String path, long fileHandle, boolean isWritepage, 
		ByteBuffer buffer, long offset) throws FuseException
	{
		_logger.trace(String.format("write(%s, %d, %s, %s, %d)",
			path, fileHandle, isWritepage, buffer, offset));
		
		try
		{
			_fs.write(fileHandle, offset, buffer);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to write to file.", 
				cause);
		}
	}
}