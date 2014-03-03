package edu.virginia.vcgr.genii.ui.shell;

public interface WordCompleter
{
	public String[] completions(String partial) throws Exception;
}