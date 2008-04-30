package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BinariesDescription
{
	private Map<String, File> _binaries;
	
	public BinariesDescription(
		Properties connectionProperties,
		String binDirProperty,
		Map<String, String> defaultPathMap)
			throws NativeQueueException
	{
		_binaries = new HashMap<String, File>(defaultPathMap.size());
		
		String binDir = connectionProperties.getProperty(binDirProperty);
		
		for (String key : defaultPathMap.keySet())
		{
			String def = defaultPathMap.get(key);
			_binaries.put(key, findBinary(binDir, 
				connectionProperties.getProperty(key, def)));
		}
	}
	
	public File get(String property)
	{
		return _binaries.get(property);
	}
	
	static private File findBinary(String binDir,
		String path) throws NativeQueueException
	{
		File ret;
		
		if (!path.contains("/"))
		{
			if (binDir != null)
				ret = new File(binDir, path);
			else
			{
				return findBinaryInPath(path);
			}
		} else
		{
			ret = new File(path);
		}
		

		if (!ret.exists())
			throw new NativeQueueException(
				"Couldn't find binary \"" + ret.getAbsolutePath() + "\".");
		if (!ret.canExecute())
			throw new NativeQueueException(
				"Binary \"" + ret.getAbsolutePath() + 
				"\" is not executable.");
		
		return ret;
	}
	
	static private File findBinaryInPath(String binaryName)
		throws NativeQueueException
	{
		String path = System.getenv("PATH");
		
		for (String dir : path.split(":"))
		{
			if (dir == null || dir.length() == 0)
				continue;
			
			File ret = new File(dir, binaryName);
			if (ret.exists() && ret.canExecute())
				return ret;
		}
		
		throw new NativeQueueException("Unable to find binary \"" + 
			binaryName + "\" in path.");
	}
}