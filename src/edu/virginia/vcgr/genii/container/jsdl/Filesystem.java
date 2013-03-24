package edu.virginia.vcgr.genii.container.jsdl;

import java.io.Serializable;

import org.ggf.jsdl.FileSystemTypeEnumeration;

public class Filesystem implements Serializable
{
	static final long serialVersionUID = 0L;

	private String _name;
	private FileSystemTypeEnumeration _type;

	protected Filesystem(String name, FileSystemTypeEnumeration type)
	{
		if (name == null)
			throw new IllegalArgumentException("File system name cannot be null.");

		if (type == null)
			throw new IllegalArgumentException("File system type cannot be null.");

		_name = name;
		_type = type;
	}

	final public String getName()
	{
		return _name;
	}

	final public FileSystemTypeEnumeration getFileSystemType()
	{
		return _type;
	}
}