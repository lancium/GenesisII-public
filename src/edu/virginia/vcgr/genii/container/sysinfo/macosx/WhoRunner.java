package edu.virginia.vcgr.genii.container.sysinfo.macosx;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;

class WhoRunner
{
	static private final Pattern WHO_PATTERN = Pattern.compile("^\\s*([^\\s]+).*$");

	private Collection<String> _loggedIn;

	private WhoRunner(Collection<String> loggedIn)
	{
		_loggedIn = loggedIn;
	}

	Collection<String> loggedIn()
	{
		return Collections.unmodifiableCollection(_loggedIn);
	}

	static WhoRunner run() throws IOException
	{
		Collection<String> loggedIn = new Vector<String>(8);
		List<String> results = ExecutionEngine.simpleMultilineExecute("who");
		for (String result : results) {
			Matcher matcher = WHO_PATTERN.matcher(result);
			if (matcher.matches())
				loggedIn.add(matcher.group(1));
		}

		return new WhoRunner(loggedIn);
	}
}