package edu.virginia.vcgr.genii.client.jsdl;

import java.io.Serializable;

public class FilesystemRelativePath implements Serializable
{
	static final long serialVersionUID = 0L;

	private String _filesystemName;
	private String _string;

	public FilesystemRelativePath(String filesystemName, String str)
	{
		_filesystemName = filesystemName;
		_string = str;
	}

	public String getString()
	{
		return _string;
	}

	public String getFileSystemName()
	{
		return _filesystemName;
	}
}