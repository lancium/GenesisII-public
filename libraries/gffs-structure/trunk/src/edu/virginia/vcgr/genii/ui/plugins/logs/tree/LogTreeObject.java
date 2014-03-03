package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

public interface LogTreeObject {
	public LogTreeObjectType objectType();

	public boolean allowsChildren();
}