package edu.virginia.vcgr.genii.ui.rns;

import edu.virginia.vcgr.genii.ui.UIContext;

public interface RNSTreeListener
{
	public void pathClicked(RNSTree tree, UIContext context,
		RNSFilledInTreeObject fObj);
	public void pathDoubleClicked(RNSTree tree, UIContext context,
		RNSFilledInTreeObject fObj);
}