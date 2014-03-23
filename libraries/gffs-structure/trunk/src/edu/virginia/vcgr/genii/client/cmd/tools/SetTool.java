package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class SetTool extends BaseGridTool
{
	static private final String DESCRIPTION = "config/tooldocs/description/dset";
	static private final String USAGE = "config/tooldocs/usage/uset";
	static private final String MANPAGE = "config/tooldocs/man/set";

	public SetTool()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), false, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		String arg = getArgument(0);
		int index = arg.indexOf('=');
		if (index <= 0)
			throw new InvalidToolUsageException("Argument not in correct format.");

		Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
		String key = arg.substring(0, index);
		String value = arg.substring(index + 1);

		if (value != null && value.length() > 0)
			env.put(key, value);
		else
			env.remove(key);

		ContextManager.storeCurrentContext(ContextManager.getExistingContext());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException("Set requires 1 argument.");
	}
}