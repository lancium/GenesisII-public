package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;

abstract class AbstractFilesystemHandle implements FilesystemHandle
{
	protected String []_path;
	protected GenesisIIFilesystem _fs;
	
	@Override 
	protected void finalize() throws Throwable
	{
		close();
	}
	
	protected AbstractFilesystemHandle(GenesisIIFilesystem fs, String []path)
	{
		_path = path;
		_fs = fs;
	}
	
	@Override
	public void delete() throws FSException
	{
		_fs.unlink(_path);
	}
	
	@Override
	public boolean renameTo(String []target)
	{
		try
		{
			_fs.rename(_path, target);
			return true;
		}
		catch (Throwable cause)
		{
			return false;
		}
	}
}