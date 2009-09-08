package edu.virginia.vcgr.genii.ui;

import javax.swing.JMenuBar;

public interface JMenuBarFactory
{
	public JMenuBar createMenuBar(UIContext uiContext);
	public void addHelpMenu(UIContext uiContext, JMenuBar bar);
}