package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class EchoCommand extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Echos the contents of the command line.";
	static final private String _USAGE =
		"echo [arg1...argn]";
	
	public EchoCommand()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		boolean first = true;
		for (String argument : getArguments())
		{
			if (!first)
				stdout.print(' ');
			first = false;
			
			stdout.print(argument);
		}
		
		stdout.println();
		stdout.flush();
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}