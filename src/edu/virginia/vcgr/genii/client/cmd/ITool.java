package edu.virginia.vcgr.genii.client.cmd;

import java.io.BufferedReader;
import java.io.PrintStream;

public interface ITool
{
	public String usage();
	public String description();
	public boolean isHidden();
	
	public void addArgument(String argument) throws ToolException;
	
	public int run(PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable;
}