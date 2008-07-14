package edu.virginia.vcgr.fuse.fs;

import java.util.Calendar;
import java.util.Collection;

import fuse.FuseException;

public interface FuseFileSystemEntry
{
	public boolean exists();
	
	public boolean isFile();
	public boolean isDirectory();
	public boolean isSymlink();
	
	public String name();
	public String pwd();
	
	public int getPermissions() throws FuseException;
	public void setPermissions(int mode) throws FuseException;
	
	public Calendar accessTime() throws FuseException;
	public void accessTime(Calendar aTime) throws FuseException;
	
	public Calendar modificationTime() throws FuseException;
	public void modificationTime(Calendar mTime) throws FuseException;
	
	public Calendar createTime() throws FuseException;
	
	public long length() throws FuseException;
	public long inode() throws FuseException;
	
	public Collection<FuseFileSystemEntry> listContents()
		throws FuseException;
	public void delete(boolean onlyUnlink) throws FuseException;
	public void mkdir(int mode) throws FuseException;
	
	public void symlink(FuseFileSystemEntry source) throws FuseException;
	public FuseFileSystemEntry readlink() throws FuseException;
	
	public FuseFile open(boolean create, boolean exclusiveCreate, 
		boolean readable, boolean writable, boolean append,
		Long truncateLength) 
			throws FuseException;
	
	public void rename(FuseFileSystemEntry source) throws FuseException;
	
	public void link(FuseFileSystemEntry source) throws FuseException;
	
	public void flush() throws FuseException;
}