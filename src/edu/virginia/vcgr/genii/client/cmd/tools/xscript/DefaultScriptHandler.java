package edu.virginia.vcgr.genii.client.cmd.tools.xscript;

import java.io.BufferedReader;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;

public class DefaultScriptHandler implements IXScriptHandler
{	
	public int handleGridCommand(String commandName, 
		String []commandLine,
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable
	{
		/*
		ToolDescription toolDesc = _tools.get(commandName);
		if (toolDesc == null)
		{
			err.println("Unable to locate grid command \"" + commandName + "\".");
			return 1;
		}
		*/
		String []cLine = new String[commandLine.length + 1];
		cLine[0] = commandName;
		for (int lcv = 0; lcv < commandLine.length; lcv++)
			cLine[lcv + 1] = commandLine[lcv];
		
		return
			new CommandLineRunner().runCommand(cLine, out, err, in);
	}
}