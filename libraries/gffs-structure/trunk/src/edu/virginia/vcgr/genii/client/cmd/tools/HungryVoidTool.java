package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class HungryVoidTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dvoid";
	static final private String _USAGE = "config/tooldocs/usage/uvoid";
	static final private String _MANPAGE = "config/tooldocs/man/void";

	/**
	 * a tool that just consumes all input and parameters passed to it. this is meant for implementing a comment or remark command.
	 */
	public HungryVoidTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		// our "action" for this command is to do nothing.
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
