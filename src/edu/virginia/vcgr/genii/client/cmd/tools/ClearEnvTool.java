package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;

public class ClearEnvTool extends BaseGridTool
{
	static final private String DESCRIPTION =
		"Clears the current environment variables.";
	static final private String USAGE =
		"clearenv";
	
	public ClearEnvTool()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GridUserEnvironment.clearGridUserEnvironment();
		ContextManager.storeCurrentContext(ContextManager.getCurrentContext());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException(
				"Too many arguments.");
	}
}