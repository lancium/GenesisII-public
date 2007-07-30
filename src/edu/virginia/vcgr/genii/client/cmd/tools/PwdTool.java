package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class PwdTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Prints the current directory.";
	static final private String _USAGE = "pwd";
	
	public PwdTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		stdout.println(RNSPath.getCurrent().pwd());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}