package edu.virginia.vcgr.genii.client.utils.urls;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods useful for working with URLs.
 * 
 * @author mmm2a
 */
public class URLUtilities
{
	static private Pattern _WINDOWS_DRIVE_PATTERN = Pattern.compile(
		"^\\p{Alpha}:(\\\\|/).*$");
	static private Pattern _SIMPLE_URL_PATTERN = Pattern.compile(
		"^\\w+:.*");
	
	/**
	 * Take a string which may represent a URL, or may represent a file path
	 * and convert it into a URL.
	 * 
	 * @param urlOrPath The string URL or Path.
	 * @param includeWindows Indicates whether or not the method should take
	 * into account Windows file paths which start with drive letters.  These
	 * paths can easily be mistaken for URLs if one isn't careful.
	 * @return The URL from the path.
	 * 
	 * @throws MalformedURLException
	 */
	static public URL formURL(String urlOrPath, boolean includeWindows)
		throws MalformedURLException
	{
		Matcher matcher;
		
		if (urlOrPath == null)
			return null;
		
		if (includeWindows)
		{
			matcher = _WINDOWS_DRIVE_PATTERN.matcher(urlOrPath);
			if (matcher.matches())
				return (new File(urlOrPath)).toURI().toURL();
		}
		
		matcher = _SIMPLE_URL_PATTERN.matcher(urlOrPath);
		if (matcher.matches())
			return new URL(urlOrPath);
		
		return (new File(urlOrPath)).toURI().toURL();
	}
}