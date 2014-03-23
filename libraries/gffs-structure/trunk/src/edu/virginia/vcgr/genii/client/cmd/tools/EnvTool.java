package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class EnvTool extends BaseGridTool
{
	static public final String USAGE = "config/tooldocs/usage/uenv";
	static public final String DESCRIPTION = "config/tooldocs/description/denv";
	static final private String _MANPAGE = "config/tooldocs/man/env";

	public EnvTool()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), true, ToolCategory.INTERNAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
		Set<String> keySet = env.keySet();
		String[] keys = new String[keySet.size()];
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
			throw new InvalidToolUsageException("Too many arguments supplied.");
	}
}
