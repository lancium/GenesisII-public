package edu.virginia.vcgr.fuse.fs;

import fuse.FuseException;

public interface FuseFileSystem
{
	public int blockSize();
	
	public int totalBlocks();
	public int blocksFree();
	
	public int totalFiles();
	public int filesFree();
	
	public int maxEntryNameLength();
	
	public FuseFileSystemEntry lookup(String path) throws FuseException;
}