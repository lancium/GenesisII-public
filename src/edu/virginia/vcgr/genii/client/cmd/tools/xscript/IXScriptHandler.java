package edu.virginia.vcgr.genii.client.cmd.tools.xscript;

import java.io.BufferedReader;
import java.io.PrintStream;

public interface IXScriptHandler
{
	public int handleGridCommand(
		String commandName,
		String []commandLIne,
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable;
}