package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import edu.virginia.vcgr.genii.ui.UIContext;

public interface LogTreeListener
{
	public void pathClicked(LogTree tree, UIContext context, LogFilledInTreeObject fObj);

	public void pathDoubleClicked(LogTree tree, UIContext context, LogFilledInTreeObject fObj);
}