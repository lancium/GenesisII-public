package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Date;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.credentials.*;
import edu.virginia.vcgr.genii.client.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

public class WhoamiTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Prints out the credentials of the currently logged in user.";
	static final private String _USAGE =
		"whoami [--verbosity={OFF|LOW|MEDIUM|HIGH}]";
	
	private VerbosityLevel _verbosity = VerbosityLevel.OFF;
	
	public void setVerbosity(String verbosityString)
		throws InvalidToolUsageException
	{
		_verbosity = VerbosityLevel.valueOf(verbosityString);
		if (_verbosity == null)
			throw new InvalidToolUsageException();
	}
	
	public WhoamiTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		
		ICallingContext callingContext = ContextManager.getCurrentContext(false);

		if (callingContext == null) 
			stdout.println("No credentials");
		else
		{
			// remove/renew stale creds/attributes
			KeyAndCertMaterial clientKeyMaterial = 
				ClientUtils.checkAndRenewCredentials(
						callingContext, 
						new Date(),
						new SecurityUpdateResults());

			TransientCredentials transientCredentials = 
				TransientCredentials.getTransientCredentials(callingContext);
			stdout.format("Client Tool Identity: \n\t%s\n\n", 
					(new X509Identity(clientKeyMaterial._clientCertChain)).describe(_verbosity));
			if (!transientCredentials._credentials.isEmpty()) 
			{
				stdout.format("Additional Credentials: \n");
				for (GIICredential cred : transientCredentials._credentials)
					stdout.format("\t%s\n", cred.describe(_verbosity));
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