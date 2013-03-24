package edu.virginia.vcgr.genii.container.exportdir.lightweight;

public class AbstractVExportEntry implements VExportEntry
{
	private String _name;
	private boolean _isDirectory;

	protected AbstractVExportEntry(String name, boolean isDirectory)
	{
		_name = name;
		_isDirectory = isDirectory;
	}

	@Override
	final public String getName()
	{
		return _name;
	}

	@Override
	final public boolean isDirectory()
	{
		return _isDirectory;
	}

	@Override
	final public boolean isFile()
	{
		return !_isDirectory;
	}
}