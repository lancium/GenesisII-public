package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import java.io.Closeable;

import edu.virginia.vcgr.fsii.exceptions.FSException;

public interface FilesystemHandle extends Closeable
{
	/* Depositions */
	static final public int SUPERSEDE = 0;		//Delete + Create
	static final public int OPEN = 1;
	static final public int CREATE = 2;
	static final public int OPEN_IF = 3;
	static final public int OVERWRITE = 4;	// Truncate
	static final public int OVERWRITE_IF = 5;
	
	/* Desired Access */
	static final public int INFORMATION_ONLY = 0;
	static final public int FILE_READ_DATA = 1;
	static final public int FILE_WRITE_DATA = 2;
	static final public int FILE_APPEND_DATA = 4;
	static final public int FILE_EXECUTE = 8; //Don't handle
	static final public int DELETE = 16;	//Don't handle
	
	static final public int INVALID_HANDLE = -1;
	
	public boolean isDirectoryHandle();
	
	public void delete() throws FSException;
	public boolean renameTo(String []target);
}