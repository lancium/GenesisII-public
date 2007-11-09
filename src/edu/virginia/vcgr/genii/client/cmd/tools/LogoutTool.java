package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;

public class LogoutTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Removes any authentication information from the user's context.";
	static final private String _USAGE =
		"logout";
	
	public LogoutTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext != null) {
			TransientCredentials.globalLogout(callContext);
			callContext.setActiveKeyAndCertMaterial(null);
			ContextManager.storeCurrentContext(callContext);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}