package edu.virginia.vcgr.genii.client.utils;

import java.io.File;
import java.util.regex.Pattern;

public class SystemExec
{
	static public File findExecutableInPath(String executableName)
	{
		String path = System.getenv("PATH");
		if (path == null)
			return null;
		
		for (String pathElement : path.split(
			Pattern.quote(File.pathSeparator)))
		{
			if (pathElement != null && pathElement.length() > 0)
			{
				File f = new File(pathElement, executableName);
				if (f.exists() && f.canExecute())
					return f;
			}
		}
		
		return null;
	}
}