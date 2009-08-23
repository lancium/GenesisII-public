package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;

public class BESWorkingDirectory
{
	private File _workingDirectory = null;
	private boolean _mustDelete = false;
	
	public BESWorkingDirectory(File workingDirectory, boolean mustDelete)
	{
		_workingDirectory = workingDirectory;
		_mustDelete = mustDelete;
	}
	
	public void setWorkingDirectory(File workingDirectory, boolean mustDelete)
	{
		_workingDirectory = workingDirectory;
		_mustDelete = mustDelete;
	}
	
	public File getWorkingDirectory()
	{
		return _workingDirectory;
	}
	
	public boolean mustDelete()
	{
		return _mustDelete;
	}
	
	@Override
	public String toString()
	{
		return _workingDirectory.toString();
	}
}