package edu.virginia.vcgr.fsii;

import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.security.Permissions;

public interface FSFilesystem
{
	public FilesystemStatStructure stat(String []path)
		throws FSException;
	public void chmod(String []path, Permissions permissions)
		throws FSException;
	public void updateTimes(String []path, 
		long accessTime, long modificationTime) throws FSException;
	
	public void rename(String []fromPath, String []toPath) throws FSException;
	public void link(String []sourcePath, String []targetPath)
		throws FSException;
	public void unlink(String []path) throws FSException;
	
	public void mkdir(String []path, Permissions initialPermissions)
		throws FSException;
	public DirectoryHandle listDirectory(String []path)
		throws FSException;
	
	public void truncate(String []path, long newSize) throws FSException;
	public long open(String []path, OpenFlags flags, OpenModes mode, 
		Permissions initialPermissions)	throws FSException;
	public void read(long fileHandle, long offset,  ByteBuffer target) 
		throws FSException;
	public void write(long fileHandle, long offset, ByteBuffer source) 
		throws FSException;
	public void flush(long fileHandle) throws FSException;
	public void close(long fileHandle) throws FSException;
}