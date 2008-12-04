package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;

abstract class AbstractFilesystemHandle implements FilesystemHandle
{
	static private Log _logger = LogFactory.getLog(
		AbstractFilesystemHandle.class);
	
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
		_logger.trace(String.format("AbstractFilesystemHandle::delete(%s)",
			UnixFilesystemPathRepresentation.INSTANCE.toString(_path)));
		
		_fs.unlink(_path);
	}
	
	@Override
	public boolean renameTo(String []target)
	{
		_logger.trace(String.format("AbstractFilesystemHandle::renameTo(%s, %s)",
			UnixFilesystemPathRepresentation.INSTANCE.toString(_path),
			UnixFilesystemPathRepresentation.INSTANCE.toString(target)));
			
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