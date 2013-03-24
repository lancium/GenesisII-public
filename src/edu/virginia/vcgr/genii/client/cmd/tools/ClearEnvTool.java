package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class ClearEnvTool extends BaseGridTool
{
	static public final String USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uclearenv";
	static public final String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dclearenv";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/clearenv";

	public ClearEnvTool()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), true, ToolCategory.INTERNAL);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GridUserEnvironment.clearGridUserEnvironment();
		ContextManager.storeCurrentContext(ContextManager.getExistingContext());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException("Too many arguments.");
	}
}