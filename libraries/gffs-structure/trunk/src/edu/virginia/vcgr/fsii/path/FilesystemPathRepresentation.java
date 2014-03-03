package edu.virginia.vcgr.fsii.path;

/**
 * This interface is used by clients and providers alike to specify how paths in their file system
 * are parsed and concatenated together to form strings.
 * 
 * @author mmm2a
 */
public interface FilesystemPathRepresentation
{
	/**
	 * Converts a path array into a rooted, String representation.
	 * 
	 * @param path
	 *            The path to convert. This parameter can be null in which case it is equivalent to
	 *            new String[0].
	 * 
	 * @return The rooted string representation of the path given.
	 */
	public String toString(String[] path);

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
	public String[] parse(String[] currentPath, String newPath);
}