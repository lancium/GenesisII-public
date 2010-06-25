package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;

public class SetTool extends BaseGridTool
{
	static final private String USAGE = "set <variable>=<value>";
	static final private String DESCRIPTION = "Set grid environment variables.";
	
	public SetTool()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String arg = getArgument(0);
		int index =  arg.indexOf('=');
		if (index <= 0)
			throw new InvalidToolUsageException(
				"Argument not in correct format.");
		
		Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
		String key = arg.substring(0, index);
		String value = arg.substring(index + 1);
		
		if (value != null && value.length() > 0)
			env.put(key, value);
		else
			env.remove(key);
		
		ContextManager.storeCurrentContext(ContextManager.getCurrentContext());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Set requires 1 argument.");
	}
}