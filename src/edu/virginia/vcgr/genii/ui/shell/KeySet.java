package edu.virginia.vcgr.genii.ui.shell;

public class KeySet
{
	private String _set;
	
	public KeySet(String set)
	{
		_set = set;
	}
	
	final public boolean inSet(char c)
	{
		return _set.indexOf(c) >= 0;
	}
}