package edu.virginia.vcgr.genii.client.rns;

import java.io.File;

/**
 * Implementation class for traversing a GeniiPath and traveling to all sub-nodes.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class JavaFileRecurser extends TreeTraverser<File>
{
  /**
   * Constructs an object for recursing a File.  This takes 4 alert
   * methods that will be invoked when (1) entering a directory, (2) leaving
   * a directory, (3) hitting a file, and (4) encountering a cycle in the
   * directory tree.  It's okay for the alert functions to be null.
   */
  public JavaFileRecurser(TreeTraversalActionAlert<File> enterDirectoryAlert, TreeTraversalActionAlert<File> exitDirectoryAlert,
      TreeTraversalActionAlert<File> fileAlert, TreeTraversalActionAlert<File> bounceAlert) {
    super(new JavaFileHierarchyHelper(), enterDirectoryAlert, exitDirectoryAlert, fileAlert, bounceAlert);
  }
  
}
