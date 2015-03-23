package edu.virginia.vcgr.genii.client;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * encapsulates one restriction (or enabling) of a path for exports for a user or the class of all
 * users.
 */
public class ExportControl
{
	static private Log _logger = LogFactory.getLog(ExportControl.class);

	private String _path = new String();
	private String _user = new String();
	private HashSet<ModeAllowance> _allowedModes = new HashSet<ModeAllowance>();

	/**
	 * this is an enumeration of the different modes available on the path. if there is no mode
	 * listed at all, then the path is unusable. similarly if the mode is DISALLOW_MODE, then the
	 * path cannot be used for exports.
	 */
	public static enum ModeAllowance {
		READ_MODE("r"),
		WRITE_MODE("w"),
		DISALLOW_MODE("NONE"),
		ALTERNATE_DISALLOW_MODE("n");

		private String _text = null;
		
		// static members know different combinations.
		static HashSet<ModeAllowance> _allModes = null;
		static HashSet<ModeAllowance> _readMode = null;
		static HashSet<ModeAllowance> _readWriteMode = null;

		private ModeAllowance(String text)
		{
			_text = text;
		}

		@Override
		public String toString()
		{
			return _text;
		}

		/**
		 * returns an ExportMechanisms enum from a string if possible. if not possible, null is
		 * returned.
		 */
		public static ModeAllowance parse(String fromString)
		{
			if (fromString == null)
				return null;
			if (fromString.equalsIgnoreCase(READ_MODE.toString()))
				return READ_MODE;
			if (fromString.equalsIgnoreCase(WRITE_MODE.toString()))
				return WRITE_MODE;
			if (fromString.equalsIgnoreCase(DISALLOW_MODE.toString()))
				return DISALLOW_MODE;
			// special case to handle people wanting to use 'n'.
			if (fromString.equalsIgnoreCase(ALTERNATE_DISALLOW_MODE.toString()))
				return DISALLOW_MODE;
			return null;
		}

		/**
		 * helper method for displaying a set of modes.
		 */
		public static String modesToString(HashSet<ModeAllowance> modes)
		{
			StringBuilder sb = new StringBuilder();
			if (modes.size() == 0) {
				sb.append(DISALLOW_MODE.toString());
			} else {
				for (ModeAllowance m : modes) {
					if (sb.length() > 0) {
						sb.append(",");
					}

					sb.append(m.toString());
				}
			}
			return sb.toString();
		}
		
		/**
		 * returns a set with all possible modes enabled.
		 */
		public static Set<ModeAllowance> getAllModes()
		{
			synchronized (ExportControl.class) {
				if (_allModes == null) {
					_allModes = new HashSet<ModeAllowance>();
					_allModes.add(ModeAllowance.READ_MODE);
					_allModes.add(ModeAllowance.WRITE_MODE);
				}
			}
			return _allModes;
		}

		public static Set<ModeAllowance> getReadOnlyMode()
		{
			synchronized (ExportControl.class) {
				if (_readMode == null) {
					_readMode = new HashSet<ModeAllowance>();
					_readMode.add(ModeAllowance.READ_MODE);
				}
			}
			return _readMode;
		}

		public static Set<ModeAllowance> getReadWriteMode()
		{
			synchronized (ExportControl.class) {
				if (_readWriteMode == null) {
					_readWriteMode = new HashSet<ModeAllowance>();
					_readWriteMode.add(ModeAllowance.READ_MODE);
					_readWriteMode.add(ModeAllowance.WRITE_MODE);
				}
			}
			return _readWriteMode;
		}


	}

	private ExportControl()
	{
		// blank constructor does not fill anything in; just for internal use.
	}

	public ExportControl(String path, String user, Set<ModeAllowance> modes)
	{
		_path = path.replace("\\", "/");
		_user = user;
		if (modes != null)
			_allowedModes = new HashSet<ModeAllowance>(modes);
		else
			_allowedModes = new HashSet<ModeAllowance>();
	}

	public String getPath()
	{
		return _path;
	}

	public String getUser()
	{
		return _user;
	}

	public Set<ModeAllowance> getModes()
	{
		return _allowedModes;
	}

	/**
	 * returns true if the user allowed by this restriction is anyone.
	 */
	public boolean allowAnyUser()
	{
		return _user.equals("*");
	}

	/**
	 * returns true if this restriction would apply to the "user" specified.
	 */
	public boolean isUserAppropriate(String user)
	{
		return _user.equals(user) || allowAnyUser();
	}

	/**
	 * return true if there are no modes, or if only DISALLOW_MODE is listed.
	 */
	public boolean nothingAllowed()
	{
		return _allowedModes.size() == 0
			|| ((_allowedModes.size() == 1) && (_allowedModes.contains(ModeAllowance.DISALLOW_MODE)));
	}

	public boolean readAllowed()
	{
		return _allowedModes.contains(ModeAllowance.READ_MODE);
	}

	public boolean writeAllowed()
	{
		return _allowedModes.contains(ModeAllowance.WRITE_MODE);
	}

	/**
	 * returns whether a particular mode request should be allowed by this restriction object
	 * (assuming user and path are appropriate).
	 */
	public boolean actionAllowed(ModeAllowance mode)
	{
		if (mode.equals(ModeAllowance.DISALLOW_MODE)) {
			/*
			 * what does this actually mean? we are interpreting it to be asking if this restriction
			 * denies all access.
			 */
			return nothingAllowed();
		} else if (mode.equals(ModeAllowance.READ_MODE)) {
			return readAllowed();
		} else if (mode.equals(ModeAllowance.WRITE_MODE)) {
			return writeAllowed();
		}
		// not sure what kind of mode got us here, so fail.
		return false;
	}

	/**
	 * combined check returns true if this restriction is appropriate for a particular user and also
	 * allows a particular mode of access.
	 */
	public boolean actionAllowedByUser(ModeAllowance mode, String user)
	{
		return isUserAppropriate(user) && actionAllowed(mode);
	}

	/**
	 * returns a positive number if the pathToCheck is rooted by the path in this restriction. if
	 * so, then this restriction may be important for the path, but otherwise this restriction has
	 * nothing to say about the pathToCheck (and zero is returned). this does not guarantee that
	 * some later restriction will not be more specific than this one, so all restrictions known
	 * must be checked. the returned number is important, because it is the number of directory
	 * components that match between the string passed in and the directory that this restriction is
	 * about; a larger number indicates more of the path matched, so when two appropriate paths are
	 * considered, the larger number is more specific for the particular path being checked. if only
	 * the root directory matches, the count is 1. if the root and one lower level directory match,
	 * then the count is 2, and so forth. this is because a match on two root directories is
	 * considered a positive result (and will not return a zero from the function).
	 */
	public int isPathAppropriate(String pathToCheck)
	{
		pathToCheck = pathToCheck.replace("\\", "/");

		boolean windowsPath = false;
		int matchCount = 0;
		if (pathToCheck.startsWith("/") && _path.startsWith("/")) {
			// we have the simplest match, both are rooted at root so neither is a relative path.
			matchCount++;
		} else {
			// windows path handling.
			if (pathToCheck.substring(1, 2).equals(":") && _path.substring(1, 2).equals(":")
				&& pathToCheck.substring(2, 3).equals("/") && _path.substring(2, 3).equals("/")
				&& pathToCheck.substring(0, 1).equalsIgnoreCase(_path.substring(0, 1))) {
				// important for C: to match c:, which is why ignores case is used above.
				matchCount++;
				windowsPath = true;
			} else {
				/*
				 * a broken call; one path is not absolute or matching drive letter, so we can't say
				 * anything about their relevance.
				 */
				return 0;
			}
		}

		// break up our path into components by slashes.
		String[] toTest = pathToCheck.split("/");
		String[] ourComponents = _path.split("/");

		int elementToTest = -1;
		for (String ourComponent : ourComponents) {
			elementToTest++;
			if (windowsPath) {
				// skip this element (which will be like "C:"), but don't skip next.
				windowsPath = false;
				continue;
			}
			if (elementToTest >= toTest.length) {
				/*
				 * this is a failure, since we ran out of components in the string to check. this
				 * means the path we have is actually longer than the one passed in, so it cannot be
				 * a match.
				 */
				return 0;
			}
			if (!ourComponent.equals(toTest[elementToTest])) {
				// not a match of the contents, which means it's not appropriate.
				return 0;
			}
		}
		if (elementToTest >= 0)
			matchCount += elementToTest;
		return matchCount;
	}

	/**
	 * reads a line from the "reader" specified, respecting comments and line continuation ('\')
	 * characters. if a line is a comment, it will be ignored and the next line will be tried. if
	 * there is a continuation character on a non-ignored line, then the next line will also be
	 * gathered, for as long as continuation characters are found at end of each line. blank lines
	 * in the file are automatically skipped. if the return value has no length, then a line could
	 * not be read from the file. this function should never return null.
	 */
	public static String readNextLine(BufferedReader reader)
	{
		/*
		 * we will set this to true when we do not care about the comment character or trimming
		 * white space.
		 */
		boolean skipLineCleaning = false;

		StringBuffer accumulator = new StringBuffer();

		while (true) {
			String currentLine = null;
			try {
				// read a line in from the file.
				currentLine = reader.readLine();
				if (currentLine == null) {
					// we're done reading; return what we've got, if anything.
					return accumulator.toString();
				}
				/*
				 * smash all white spaces into a single white space. that's fine even if we aren't
				 * supposed to be cleaning, since this should not change the string significantly;
				 * it just replaces double spaces and tabs and such with a single space.
				 */
				currentLine = currentLine.replaceAll("\\s+", " ");
			} catch (Exception e) {
				// bail out here; we couldn't read anything.
				if (_logger.isDebugEnabled())
					_logger.debug("exception while reading file", e);
				return accumulator.toString();
			}

			if (!skipLineCleaning) {
				/*
				 * chop all whitespace at front and end of line. this could incorrectly tell us a
				 * backslash was actually at end when there were spaces after it, but such a line is
				 * already illegal based on the expected syntax (there should be a user name or a
				 * set of option characters instead of a backslash). at worse we would still handle
				 * a backslash with a goofed up space after it for continuation.
				 */
				currentLine = currentLine.trim();

				// is first character a comment? if so, drop line, and get next.
				if (currentLine.startsWith("#")) {
					continue;
				}
			}

			// now that we handled any request to not clean up the line, flip that flag off again.
			skipLineCleaning = false;

			if (currentLine.length() == 0) {
				if (accumulator.length() > 0) {
					/*
					 * they could have had a continuation and a blank line after; if so, we'll
					 * return the line now.
					 */
					break;
				}

				// this line is useless (blank) so we'll skip it.
				continue;
			}

			// line is not a comment, so is the last character requesting line continuation?
			if (currentLine.endsWith("\\")) {
				/*
				 * for continuation, put current line in buffer sans backslash, and read next line.
				 * next line after continuation cannot be a comment, so we should skip the comment
				 * check in that case.
				 */
				skipLineCleaning = true;
				accumulator.append(currentLine.substring(0, currentLine.length() - 1));
				continue;
			}

			// if no continuation, add line to buffer, return current buffer.
			accumulator.append(currentLine);
			break; // done with looping since we should have a good string.
		}
		return accumulator.toString();
	}

	/**
	 * parses an export restriction line in the expected format:
	 * 
	 * PATH USER [OPTIONS]
	 * 
	 * OPTIONS can be chosen from: r (read), w (write), n (none) OPTIONS are jammed together without
	 * intervening characters. it does not make sense to have other options together with 'n' for
	 * none.
	 * 
	 * if a PATH has no option next to it, then there are no exports allowed on that path (or on
	 * paths found under that path).
	 * 
	 * USER can be a user name or "*" for all users.
	 * 
	 * null is returned if the line cannot be parsed properly.
	 */
	public static ExportControl parseLine(String line)
	{
		// condense all the white space sequences down to a single space character.
		line = line.replaceAll("\\s+", " ");
		// remove any spaces on front and back so we split correctly.
		line = line.trim();

		String[] parts = line.split(" ");
		if (parts.length < 2) {
			_logger.error("invalid export restriction line found; does not have enough fields (at least 2 are required): "
				+ line);
		}
		if (parts.length > 3) {
			_logger.error("invalid export restriction line found; has too many fields (3 max): " + line);
		}

		ExportControl toReturn = new ExportControl();
		toReturn._path = parts[0];
		toReturn._user = parts[1];
		toReturn._allowedModes = new HashSet<ModeAllowance>();

		if (parts.length > 2) {
			// we can check out the mode they gave us, since there's something there.
			if (parts[2].length() == 0) {
				toReturn._allowedModes.add(ModeAllowance.DISALLOW_MODE);
			} else {
				for (char c : parts[2].toCharArray()) {
					ModeAllowance found = ModeAllowance.parse(new String(new char[] { c }));
					if (found != null) {
						toReturn._allowedModes.add(found);
					} else {
						_logger.error("failure in parsing modes from: '" + parts[2] + "'");
					}
				}
			}
		}
		return toReturn;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("path '");
		sb.append(_path);
		sb.append("', ");
		sb.append("user '");
		sb.append(_user);
		sb.append("', ");
		sb.append("modes [");
		sb.append(ModeAllowance.modesToString(_allowedModes));
		sb.append("]");
		return sb.toString();
	}
}
