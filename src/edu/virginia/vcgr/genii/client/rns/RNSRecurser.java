package edu.virginia.vcgr.genii.client.rns;

/**
 * Implementation class for traversing an RNS path and traveling to all sub-nodes.
 *
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class RNSRecurser extends TreeTraverser<RNSPath>
{
  /**
   * Constructs an object for recursing an RNS path.  This takes 4 alert
   * methods that will be invoked when (1) entering a directory, (2) leaving
   * a directory, (3) hitting a file, and (4) encountering a cycle in the
   * RNS hierarchy graph.  It's okay for the alert functions to be null.
   */
  public RNSRecurser(TreeTraversalActionAlert<RNSPath> enterDirectoryAlert, TreeTraversalActionAlert<RNSPath> exitDirectoryAlert,
      TreeTraversalActionAlert<RNSPath> fileAlert, TreeTraversalActionAlert<RNSPath> bounceAlert) {
    super(new RNSPathHierarchyHelper(), enterDirectoryAlert, exitDirectoryAlert, fileAlert, bounceAlert);
  }
  
}
