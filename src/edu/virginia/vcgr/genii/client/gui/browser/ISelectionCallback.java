package edu.virginia.vcgr.genii.client.gui.browser;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * The selection callback interface is used internally so that GUI actions can find out which RNS
 * paths have been selected.
 * 
 * @author mmm2a
 */
interface ISelectionCallback
{
	/**
	 * Return the list of currently selected RNS paths (or null or empty).
	 * 
	 * @return The list of currently selected RNS paths.
	 */
	public RNSPath[] getSelectedPaths();
}