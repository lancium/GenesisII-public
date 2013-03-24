package edu.virginia.vcgr.genii.ui.rns;

import java.io.File;
import java.util.List;

public class FilesystemOperatorSource implements OperatorSource
{
	private List<File> _sources;

	public FilesystemOperatorSource(List<File> sources)
	{
		_sources = sources;
	}

	@Override
	public boolean isFilesystemSource()
	{
		return true;
	}

	@Override
	public boolean isRNSSource()
	{
		return false;
	}

	public List<File> sources()
	{
		return _sources;
	}
}