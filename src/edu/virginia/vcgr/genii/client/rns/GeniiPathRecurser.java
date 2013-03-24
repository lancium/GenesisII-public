package edu.virginia.vcgr.genii.client.rns;

import edu.virginia.vcgr.genii.client.gpath.GeniiPath;

/**
 * Implementation class for traversing a GeniiPath and traveling to all sub-nodes.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class GeniiPathRecurser extends TreeTraverser<GeniiPath>
{
	/**
	 * Constructs an object for recursing a GeniiPath. This takes 4 alert methods that will be
	 * invoked when (1) entering a directory, (2) leaving a directory, (3) hitting a file, and (4)
	 * encountering a cycle in the directory tree. It's okay for the alert functions to be null.
	 */
	public GeniiPathRecurser(TreeTraversalActionAlert<GeniiPath> enterDirectoryAlert,
		TreeTraversalActionAlert<GeniiPath> exitDirectoryAlert, TreeTraversalActionAlert<GeniiPath> fileAlert,
		TreeTraversalActionAlert<GeniiPath> bounceAlert)
	{
		super(new GeniiPathHierarchyHelper(), enterDirectoryAlert, exitDirectoryAlert, fileAlert, bounceAlert);
	}
}
