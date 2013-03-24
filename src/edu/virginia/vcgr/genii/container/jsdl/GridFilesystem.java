package edu.virginia.vcgr.genii.container.jsdl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ggf.jsdl.FileSystemTypeEnumeration;

public class GridFilesystem extends Filesystem
{
	static final long serialVersionUID = 0L;

	static final public Pattern MOUNT_SOURCE_PATTERN = Pattern.compile("^rns:(.+)$");

	static final public FileSystemTypeEnumeration FILESYSTEM_TYPE = FileSystemTypeEnumeration.normal;

	private String _mountSourcePath;

	GridFilesystem(String filesystemName, String mountSourceURI)
	{
		super(filesystemName, FILESYSTEM_TYPE);

		Matcher matcher = MOUNT_SOURCE_PATTERN.matcher(mountSourceURI);
		if (!matcher.matches())
			throw new IllegalArgumentException(String.format("Mount source \"%s\" is not a valid grid mount source.",
				mountSourceURI));

		_mountSourcePath = matcher.group(1);
	}

	final public String getGridMountSourcePath()
	{
		return _mountSourcePath;
	}

	@Override
	public String toString()
	{
		return String.format("rns:%s", _mountSourcePath);
	}
}