package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;

public class WhoamiTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Prints out the credentials of the currently logged in user.";
	static final private String _USAGE =
		"whoami";
	
	public WhoamiTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		// get the signed GAML assertion 
		ICallingContext callingContext = ContextManager.getCurrentContext();
		if (callingContext == null) {
			stdout.println("Not logged in");
		} else {
			TransientCredentials transientCredentials = 
				TransientCredentials.getTransientCredentials(callingContext);
			if (transientCredentials._credentials.isEmpty()) {
				stdout.println("Not logged in");
			} else {
				for (GamlCredential cred : transientCredentials._credentials) {
					stdout.println(cred.toString());
				}
			}
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