package edu.virginia.vcgr.genii.client.gui.browser.grid;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface IActionContext
{
	public void performLongRunningAction(ILongRunningAction action);
	
	public void refreshSubTree(RNSPath subtreePath);
	
	public void reportError(String msg);
	public void reportError(String msg, Throwable cause);
}