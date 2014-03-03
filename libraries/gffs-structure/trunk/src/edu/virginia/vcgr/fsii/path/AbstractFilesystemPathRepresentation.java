package edu.virginia.vcgr.fsii.path;

import java.util.Stack;

public abstract class AbstractFilesystemPathRepresentation implements FilesystemPathRepresentation
{
	/**
	 * Retrieve the string representation of the root of this file system.
	 * 
	 * @return The string representation of the root of this file system.
	 */
	abstract protected String getRootString();

	/**
	 * Converts the rooted string path array into a string representation of the path.
	 * 
	 * @param path
	 *            The rooted path array to convert into a string representation.
	 * 
	 * @return The string representation of the rooted path array.
	 */
	abstract protected String toStringImpl(String[] path);

	/**
	 * Given a path string, split that string into its constituant parts. This is NOT the same as
	 * converting it into a rooted path array for two reasons. One, the path doesn't have to be
	 * rooted. And two, the array can contain empty strings (often the case when using the
	 * String.split function) as well as . and .. elements.
	 * 
	 * @param path
	 *            The path to split into a string array.
	 * @return The split'ted string.
	 */
	abstract protected String[] splitPath(String path);

	/**
	 * Indicates whether the given String represents a rooted (or absolute) path.
	 * 
	 * @param path
	 *            The path to test for rooted'ness.
	 * 
	 * @return True if the path given is rooted or absolute, false otherwise.
	 */
	abstract protected boolean isRooted(String path);

	/**
	 * Parses a given path (relative to another rooted path) into a string array.
	 * 
	 * @param currentPath
	 *            The current rooted path to parse the new path relative to.
	 * @param newPath
	 *            The new path string to parse. This can be absolute or relative.
	 * 
	 * @return The newly parsed path array. This is guaranteed to be non-null.
	 */
	protected String[] parseImpl(String[] currentPath, String newPath)
	{
		Stack<String> ret = new Stack<String>();

		if (!isRooted(newPath)) {
			for (String element : currentPath) {
				if (element.equals("."))
					continue;
				if (element.equals("..")) {
					if (!ret.isEmpty())
						ret.pop();
					continue;
				}

				ret.push(element);
			}
		}

		for (String element : splitPath(newPath)) {
			if ((element.length() == 0) || (element.equals(".")))
				continue;
			else if (element.equals("..") && !ret.isEmpty())
				ret.pop();
			else
				ret.push(element);
		}

		return ret.toArray(new String[0]);
	}

	@Override
	/** {@inheritDoc} */
	final public String[] parse(String[] currentPath, String newPath)
	{
		if (currentPath == null)
			currentPath = new String[0];

		if (newPath == null)
			newPath = ".";

		String[] result = parseImpl(currentPath, newPath);
		if (result == null)
			result = new String[0];

		return result;
	}

	@Override
	/** {@inheritDoc} */
	final public String toString(String[] path)
	{
		if (path == null)
			path = new String[0];

		String ret = toStringImpl(path);
		if (ret == null)
			ret = getRootString();

		return ret;
	}
}