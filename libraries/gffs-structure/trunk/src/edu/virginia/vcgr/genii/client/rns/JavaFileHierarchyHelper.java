package edu.virginia.vcgr.genii.client.rns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper routines for directory tree traversals on GeniiPath paths.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class JavaFileHierarchyHelper implements TreeTraversalPathQuery<File>
{
	static private Log _logger = LogFactory.getLog(JavaFileHierarchyHelper.class);

	public JavaFileHierarchyHelper()
	{
	}

	@Override
	public PathOutcome checkPathSanity(File path, TreeTraversalActionAlert<File> bounce)
	{
		if (path == null)
			return PathOutcome.OUTCOME_NOTHING;
		// now check some characteristics of this node.
		if (!path.exists()) {
			_logger.warn("path no longer exists: " + path.toString());
			return PathOutcome.OUTCOME_NONEXISTENT;
		}

		// for cycle detection, we rely on the tree traverser's depth limit. currently there is
		// no good way in java6 to tell if a file is a link.

		return PathOutcome.OUTCOME_SUCCESS;
	}

	@Override
	public boolean exists(File path)
	{
		if (path == null)
			return false;
		return path.exists();
	}

	@Override
	public boolean isDirectory(File path)
	{
		if (path == null)
			return false;
		return path.isDirectory();
	}

	@Override
	public boolean isFile(File path)
	{
		if (path == null)
			return false;
		return path.isFile();
	}

	@Override
	public Collection<File> getContents(File path)
	{
		if (path == null)
			return null;
		ArrayList<File> arr = new ArrayList<File>(0);
		try {
			File[] filesFound = path.listFiles();
			for (int i = 0; i < filesFound.length; i++) {
				arr.add(new File(filesFound[i].getAbsolutePath().replace('\\', '/')));
			}
		} catch (Throwable cause) {
			_logger.error("failed to list path contents for " + path.toString(), cause);
		}
		if (_logger.isDebugEnabled()) {
			_logger.debug("returning an array with " + arr.size() + " elems:");
			for (int i = 0; i < arr.size(); i++) {
				_logger.debug("index " + i + ": " + arr.get(i).toString());
			}
		}
		return arr;
	}
}
