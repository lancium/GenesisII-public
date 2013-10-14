package edu.virginia.vcgr.appmgr.patch.builder.diffengine;

import java.io.File;

public class DirCompContext
{
	private DifferenceHandler _handler;
	private File _baseDirOne;
	private File _baseDirTwo;

	public DirCompContext(DifferenceHandler handler, File baseDirOne, File baseDirTwo)
	{
		_handler = handler;
		_baseDirOne = baseDirOne;
		_baseDirTwo = baseDirTwo;
	}

	public DifferenceHandler handler()
	{
		return _handler;
	}

	public FileTuple getRelativeFile(String relativePath)
	{
		return new FileTuple(new File(_baseDirOne, relativePath), new File(_baseDirTwo, relativePath));
	}

	public File getBaseOne()
	{
		return _baseDirOne;
	}

	public File getBaseTwo()
	{
		return _baseDirTwo;
	}
}