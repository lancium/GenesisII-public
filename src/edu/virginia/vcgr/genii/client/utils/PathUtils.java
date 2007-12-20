package edu.virginia.vcgr.genii.client.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils
{
	static private Pattern windowsRootPathPattern = Pattern.compile("^[a-zA-Z]:\\.*");
	
	static public URI pathToURI(String path) throws URISyntaxException
	{
		if (path == null)
			return null;
		
		if (File.separatorChar == '\\')
		{
			Matcher matcher = windowsRootPathPattern.matcher(path);
			if (matcher.matches())
				return new File(path).toURI();
		}
		
		if (path.contains(":"))
			return new URI(path);
		
		return new File(path).toURI();
	}
}