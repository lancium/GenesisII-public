package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class PathVariable
{
	static public enum FindTypes
	{
		FILE,
		DIRECTORY,
		ANY
	}
	
	private Collection<File> _paths;
	
	private PathVariable(String pathString)
	{
		String []elements = pathString.split(":");
		
		_paths = new ArrayList<File>(elements.length);
		for (String e : elements)
		{
			if (e.length() != 0)
				_paths.add(new File(e));
		}
	}
	
	public Collection<File> pathElements()
	{
		return _paths;
	}
	
	public File find(String filename, FindTypes findType)
	{
		File f = new File(filename);
		if (f.isAbsolute())
		{
			if (findType == FindTypes.ANY)
				return f;
			else if (findType == FindTypes.FILE && f.isFile())
				return f;
			else if (findType == FindTypes.DIRECTORY && f.isDirectory())
				return f;
			else
				return null;
		}
		
		for (File dir : _paths)
		{
			f = new File(dir, filename);
			if (f.exists())
			{
				if (findType == FindTypes.ANY)
					return f;
				else if (findType == FindTypes.FILE && f.isFile())
					return f;
				else if (findType == FindTypes.DIRECTORY && f.isDirectory())
					return f;
			}
		}
		
		return null; 
	}
	
	static public PathVariable createVariable(String pathString)
	{
		return new PathVariable(pathString);
	}
	
	static public PathVariable lookupVariable(Properties properties, 
		String pathVariableName)
	{
		String value = properties.getProperty(pathVariableName, "");
		return new PathVariable(value);
	}
}