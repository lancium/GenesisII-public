package edu.virginia.vcgr.genii.client.machine;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;

class LinuxMachineInterrogator extends CommonMachineInterrogator
{
	static private Log _logger = LogFactory.getLog(LinuxMachineInterrogator.class);

	@Override
	public boolean canDetermineUserLoggedIn()
	{
		return true;
	}

	@Override
	public boolean isUserLoggedIn()
	{
		try {
			return getWhoLoggedIn().size() > 0;
		} catch (IOException ioe) {
			_logger.error("Exception thrown while trying to " + "determine whether or not a user is logged in.", ioe);

			return false;
		}
	}

	static private final Pattern _WHO_PATTERN = Pattern.compile("^(\\S+)\\s+.*$");

	static public Collection<String> getWhoLoggedIn() throws IOException
	{
		TreeSet<String> ret = new TreeSet<String>();

		for (String line : ExecutionEngine.simpleMultilineExecute("who")) {
			Matcher matcher = _WHO_PATTERN.matcher(line);
			if (!matcher.matches()) {
				System.err.println("Line \"" + line + "\" does not match.");
			} else {
				ret.add(matcher.group(1));
			}
		}

		return ret;
	}
}