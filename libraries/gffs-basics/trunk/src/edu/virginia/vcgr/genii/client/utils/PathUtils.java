package edu.virginia.vcgr.genii.client.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PathUtils
{
	static private Pattern windowsRootPathPattern = Pattern.compile("^[a-zA-Z]:\\\\.*");
	static private Log _logger = LogFactory.getLog(PathUtils.class);

	static public URI pathToURI(String path) throws URISyntaxException
	{
		if (path == null)
			return null;

		if (File.separatorChar == '\\') {
			Matcher matcher = windowsRootPathPattern.matcher(path);
			if (matcher.matches()) {
				if (_logger.isTraceEnabled())
					_logger.trace("dos path matcher saw a match on: " + path);
				return new File(path).toURI();
			}
		}

		if (path.contains(":")) {
			if (_logger.isTraceEnabled())
				_logger.trace("path contained a colon, so doing a new URI: " + path);
			return new URI(path);
		}

		if (_logger.isTraceEnabled())
			_logger.trace("normal approach just using bare path plus 'rns:' modifier for: " + path);
		return new URI("rns:" + path);
	}
}