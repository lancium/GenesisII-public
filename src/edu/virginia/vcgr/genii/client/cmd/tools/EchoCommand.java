package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class EchoCommand extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/decho";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uecho";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/echo";

	public EchoCommand()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		boolean first = true;
		for (String argument : getArguments()) {
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

	@Override
	public void addArgument(String argument)
	{
		_arguments.add(argument);
	}
}