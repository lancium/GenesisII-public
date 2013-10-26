package edu.virginia.vcgr.appmgr.patch.builder.diffengine;

import java.io.File;

public class FileTuple
{
	private File _one;
	private File _two;

	public FileTuple(File one, File two)
	{
		_one = one;
		_two = two;
	}

	public File one()
	{
		return _one;
	}

	public File two()
	{
		return _two;
	}
}