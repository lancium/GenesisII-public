package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;


public class DirectoryHandle extends AbstractFilesystemHandle
{
	static private Log _logger = LogFactory.getLog(DirectoryHandle.class);
	
	public DirectoryHandle(GenesisIIFilesystem fs, String []path)
	{
		super(fs, path);
	}
	
	@Override
	public boolean isDirectoryHandle()
	{
		return true;
	}

	@Override
	synchronized public void close() throws IOException
	{
	}
	
	public Iterable<FilesystemStatStructure> listEntries() throws FSException
	{
		_logger.trace(String.format("DirectoryHandle::listEntries(%s)",
			UnixFilesystemPathRepresentation.INSTANCE.toString(_path)));
		
		return _fs.listDirectory(_path);
	}
}