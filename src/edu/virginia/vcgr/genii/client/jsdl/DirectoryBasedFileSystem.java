package edu.virginia.vcgr.genii.client.jsdl;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.io.FileSystemUtils;

public class DirectoryBasedFileSystem extends AbstractJSDLFileSystem
{
	static final long serialVersionUID = 0L;
	
	protected File _directory;
	
	public DirectoryBasedFileSystem(File directory)
	{
		_directory = directory;
	}
	
	@Override
	protected File relativeToImpl(String relativePath) throws IOException
	{
		return new File(_directory, relativePath);
	}
	
	protected boolean shouldDestroy()
	{
		return true;
	}
	
	@Override
	public void release()
	{
		if (shouldDestroy())
			FileSystemUtils.recursiveDelete(_directory, false);
	}
}