package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;

public class EnvTool extends BaseGridTool
{
	static public final String USAGE = "env";
	static public final String DESCRIPTION = 
		"Prints the contents of the user's environment.";
	
	public EnvTool()
	{
		super(DESCRIPTION, USAGE, true);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
		Set<String> keySet = env.keySet();
		String []keys = new String[keySet.size()];
		keySet.toArray(keys);
		Arrays.sort(keys);
		for (String key : keys)
			stdout.format("%s=%s\n", key, env.get(key));

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException(
				"Too many arguments supplied.");
	}
}
