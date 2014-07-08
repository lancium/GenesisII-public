package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class WhoamiTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dwhoami";
	static final private String _USAGE = "config/tooldocs/usage/uwhoami";
	static final private String _MANPAGE = "config/tooldocs/man/whoami";

	private VerbosityLevel _verbosity = VerbosityLevel.OFF;
	// true if we want to show openssl one-line rdn format.
	private boolean _oneLine = false;

	@Option({ "verbosity" })
	public void setVerbosity(String verbosityString) throws InvalidToolUsageException
	{
		_verbosity = VerbosityLevel.valueOf(verbosityString);
		if (_verbosity == null)
			throw new InvalidToolUsageException();
	}

	@Option({ "oneline" })
	public void setOneline()
	{
		_oneLine = true;
	}

	public WhoamiTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{

		ICallingContext callingContext = ContextManager.getCurrentContext();

		if (callingContext == null)
			stdout.println("No credentials");
		else {
			// remove/renew stale creds/attributes
			KeyAndCertMaterial clientKeyMaterial =
				ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(),
					new SecurityUpdateResults());

			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callingContext);
			stdout.format("Client Tool Identity: \n\t%s\n", (new X509Identity(clientKeyMaterial._clientCertChain,
				IdentityType.CONNECTION)).describe(_verbosity));
			if (_oneLine) {
				stdout.format("\t%s\n", X509Identity.getOpensslRdn(clientKeyMaterial._clientCertChain[0]));
			}
			stdout.format("\n");
			if (!transientCredentials.isEmpty()) {
				stdout.format("Additional Credentials: \n");
				for (NuCredential cred : transientCredentials.getCredentials()) {
					stdout.format("\t%s\n", cred.describe(_verbosity));
					if (_oneLine) {
						stdout.format("\t%s\n", X509Identity.getOpensslRdn(cred.getOriginalAsserter()[0]));
					}
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