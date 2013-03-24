package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Date;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class WhoamiTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dwhoami";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uwhoami";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/whoami";

	private VerbosityLevel _verbosity = VerbosityLevel.OFF;

	@Option({ "verbosity" })
	public void setVerbosity(String verbosityString) throws InvalidToolUsageException
	{
		_verbosity = VerbosityLevel.valueOf(verbosityString);
		if (_verbosity == null)
			throw new InvalidToolUsageException();
	}

	public WhoamiTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{

		ICallingContext callingContext = ContextManager.getCurrentContext();

		if (callingContext == null)
			stdout.println("No credentials");
		else {
			// remove/renew stale creds/attributes
			KeyAndCertMaterial clientKeyMaterial = ClientUtils.checkAndRenewCredentials(callingContext, new Date(),
				new SecurityUpdateResults());

			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callingContext);
			stdout.format("Client Tool Identity: \n\t%s\n\n", (new X509Identity(clientKeyMaterial._clientCertChain,
				IdentityType.CONNECTION)).describe(_verbosity));
			if (!transientCredentials.isEmpty()) {
				stdout.format("Additional Credentials: \n");
				for (NuCredential cred : transientCredentials.getCredentials())
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