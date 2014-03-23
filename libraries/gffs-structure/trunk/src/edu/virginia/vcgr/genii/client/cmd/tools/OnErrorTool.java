package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class OnErrorTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/donerror";
	static final private String _USAGE = "config/tooldocs/usage/uonerror";
	static final private String _MANPAGE = "config/tooldocs/man/onerror";

	static private Log _logger = LogFactory.getLog(OnErrorTool.class);

	public OnErrorTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		// if no previous error, then no harm no foul and no error now.
		if (getLastExit() == 0)
			return 0;

		StringBuilder errorMsg = new StringBuilder();
		errorMsg.append("OnError--exiting due to failure: ");
		for (String argument : getArguments()) {
			errorMsg.append(argument);
			errorMsg.append(' ');
		}
		stderr.println(errorMsg);
		stderr.flush();
		_logger.error(errorMsg);
		// we should not get to the return...
		System.exit(1);
		// old, problem? getLastExit());
		_logger.error("somehow zombying onward instead of exiting!");
		System.exit(1);
		return getLastExit();
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
