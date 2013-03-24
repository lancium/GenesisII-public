package edu.virginia.vcgr.genii.client.rns;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

/**
 * Helper routines for directory tree traversals on GeniiPath paths.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class GeniiPathHierarchyHelper implements TreeTraversalPathQuery<GeniiPath>
{
	static private Log _logger = LogFactory.getLog(GeniiPathHierarchyHelper.class);

	public GeniiPathHierarchyHelper()
	{
	}

	@Override
	public PathOutcome checkPathSanity(GeniiPath path, TreeTraversalActionAlert<GeniiPath> bounce)
	{
		if (path == null)
			return PathOutcome.OUTCOME_NOTHING;
		// now check some characteristics of this node.
		if (!path.exists()) {
			_logger.warn("path no longer exists: " + path.toString());
			return PathOutcome.OUTCOME_NONEXISTENT;
		}
		// check that the path is okay for our purposes if it's actually a grid path.
		RNSPath tempRNS = path.lookupRNS();
		if (tempRNS != null) {
			PathOutcome test = RNSPathHierarchyHelper.checkPathIsNormal(tempRNS);
			if (PathOutcome.OUTCOME_SUCCESS.differs(test))
				return test;
		}
		return PathOutcome.OUTCOME_SUCCESS;
	}

	@Override
	public boolean exists(GeniiPath path)
	{
		if (path == null)
			return false;
		return path.exists();
	}

	@Override
	public boolean isDirectory(GeniiPath path)
	{
		if (path == null)
			return false;
		return path.isDirectory();
	}

	@Override
	public boolean isFile(GeniiPath path)
	{
		if (path == null)
			return false;
		return path.isFile();
	}

	@Override
	public Collection<GeniiPath> getContents(GeniiPath path)
	{
		if (path == null)
			return null;
		ArrayList<GeniiPath> arr = new ArrayList<GeniiPath>(0);
		try {
			if (path.pathType() == GeniiPathType.Grid) {
				RNSPath curr = RNSPath.getCurrent();
				for (RNSPath file : curr.expand(path.path() + "/*")) {
					if (file.exists()) {
						if (_logger.isDebugEnabled())
							_logger.debug("getContents adding GeniiPath of " + file.toString());
						arr.add(new GeniiPath(file.toString()));
					}
				}
			} else {
				File[] filesFound = (new File(path.path())).listFiles();
				for (int i = 0; i < filesFound.length; i++) {
					arr.add(new GeniiPath("local:" + filesFound[i].getAbsolutePath().replace('\\', '/')));
				}
			}
		} catch (Throwable cause) {
			_logger.error("failed to list path contents for " + path.toString(), cause);
		}
		if (_logger.isDebugEnabled()) {
			if (_logger.isDebugEnabled())
				_logger.debug("returning an array with " + arr.size() + " elems:");
			for (int i = 0; i < arr.size(); i++) {
				if (_logger.isDebugEnabled())
					_logger.debug("index " + i + ": " + arr.get(i).toString());
			}
		}
		return arr;
	}
}
