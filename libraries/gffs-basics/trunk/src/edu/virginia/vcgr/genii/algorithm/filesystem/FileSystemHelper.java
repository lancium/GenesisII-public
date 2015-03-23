package edu.virginia.vcgr.genii.algorithm.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemHelper
{
	private static Log _logger = LogFactory.getLog(FileSystemHelper.class);

	/**
	 * returns true if the path contains a symbolic link anywhere within it.
	 */
	public static boolean pathContainsLink(String path) throws FileNotFoundException
	{
		// replace any backslashes with forward slashes.
		path = path.replaceAll("\\+", "/");

		// make sure path is absolute.
		if (!path.startsWith("/")) {
			String msg = "path passed in was not absolute: '" + path + "'";
			_logger.error(msg);
			throw new FileNotFoundException(msg);
		}

		// replace any double slashes with single ones.
		path = path.replaceAll("//", "/");
		String[] components = path.split("/");

		String currentPath = ""; // never expected to be a link.
		for (String component : components) {
			currentPath = currentPath + "/" + component;
			if (isFileSymLink(new File(currentPath))) {
				return true;
			}
		}
		return false;

		/*
		 * future: this could be more useful if it returned the position of the link as a component in
		 * path, but then we also need to accept a starting point for the link searching so they can
		 * find all of them.
		 */
	}

	/**
	 * returns true if the path specified is actually a symbolic link.
	 */
	public static boolean isFileSymLink(File path)
	{
		Path nioPath = path.toPath();
		return Files.isSymbolicLink(nioPath);
	}

}
