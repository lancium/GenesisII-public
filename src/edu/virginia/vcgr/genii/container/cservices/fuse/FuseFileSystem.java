package edu.virginia.vcgr.genii.container.cservices.fuse;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

class FuseFileSystem implements JSDLFileSystem
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(FuseFileSystem.class);

	private File _mountPoint;
	
	FuseFileSystem(File mountPoint)
	{
		_mountPoint = mountPoint;
	}
	
	@Override
	final public File relativeTo(String relativePath) throws IOException
	{
		return new File(_mountPoint, relativePath);
	}

	@Override
	final public void release()
	{
		try
		{
			FuseFilesystemService ffs = ContainerServices.findService(
				FuseFilesystemService.class);
			ffs.release(_mountPoint);
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to release swap file system.", cause);
		}
	}

	@Override
	final public File getMountPoint()
	{
		return _mountPoint;
	}
}