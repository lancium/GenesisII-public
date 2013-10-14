package edu.virginia.vcgr.genii.client.utils.file;

import java.io.File;
import java.io.FileFilter;

public class ExtensionFileFilter implements FileFilter
{
	private String _extension;

	public ExtensionFileFilter(String extension)
	{
		if (extension == null)
			throw new IllegalArgumentException("Extension string cannot be null.");

		int index = extension.lastIndexOf('.');
		if (index >= 0)
			_extension = extension.substring(index);
		else
			_extension = "." + extension;
	}

	@Override
	final public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(_extension);
	}

	static public ExtensionFileFilter XML = new ExtensionFileFilter("xml");
	static public ExtensionFileFilter PROPERTIES = new ExtensionFileFilter("properties");
}