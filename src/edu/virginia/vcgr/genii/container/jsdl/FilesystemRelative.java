package edu.virginia.vcgr.genii.container.jsdl;

import java.io.Serializable;
import java.util.Map;

public class FilesystemRelative<Type> implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _filesystemName;
	private Type _target;
	
	public FilesystemRelative(String filesystemName, Type target)
	{
		if (filesystemName == null && target == null)
			throw new IllegalArgumentException(
				"Filesystem Name and Target cannot both be null.");
		
		_filesystemName = filesystemName;
		_target = target;
	}
	
	final public Filesystem getFilesystem(
		final Map<String, Filesystem> filesystems)
	{
		return filesystems.get(_filesystemName);
	}
	
	final public Type getTarget()
	{
		return _target;
	}
	
	@Override
	public String toString()
	{
		if (_filesystemName != null)
		{
			if (_target != null)
				return String.format("[%s] %s", _filesystemName, _target);
			else
				return String.format("[%s]", _filesystemName);
		} else
			return _target.toString();
	}
}