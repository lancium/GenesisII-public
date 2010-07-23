package edu.virginia.vcgr.genii.ui.shell;

import java.io.Reader;

public interface ExecutionContext
{
	public WordCompleter commandCompleter();
	public WordCompleter pathCompleter();
	public WordCompleter optionCompleter();
	
	public void executeCommand(String commandLine,
		Display display, Reader stdin) throws Exception;
}