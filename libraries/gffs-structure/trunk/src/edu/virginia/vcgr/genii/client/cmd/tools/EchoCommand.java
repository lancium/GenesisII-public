package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class EchoCommand extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/decho";
	static final private String _USAGE = "config/tooldocs/usage/uecho";
	static final private String _MANPAGE = "config/tooldocs/man/echo";

	public EchoCommand()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
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