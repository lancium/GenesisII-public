package edu.virginia.vcgr.genii.client.gui.browser.plugins.rns;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used internally by the NewDirectoryDialog to ensure that the directory name entered
 * into the text field contains the appropriate characters only.
 * 
 * @author mmm2a
 */
public class NewDirectoryName
{
	static private Pattern _validDirectoryNamePattern = Pattern.compile("^[a-zA-Z0-9 .]*$");

	private String _directoryName;

	/**
	 * Create a new directory name with the appropriate characters for an RNS name.
	 * 
	 * @param name
	 *            The new directory name to create a directory name object from.
	 * 
	 * @throws ParseException
	 */
	public NewDirectoryName(String name) throws ParseException
	{
		Matcher matcher = _validDirectoryNamePattern.matcher(name);
		if (!matcher.matches())
			throw new ParseException("Invalid characters in directory name.", 0);
		_directoryName = name;
	}

	@Override
	public String toString()
	{
		return _directoryName;
	}
}