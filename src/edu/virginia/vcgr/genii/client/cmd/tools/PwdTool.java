package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class PwdTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dpwd";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/upwd";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/pwd";

	public PwdTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
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