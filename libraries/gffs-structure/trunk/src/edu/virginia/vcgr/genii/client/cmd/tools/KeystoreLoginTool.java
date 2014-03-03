package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.login.AbstractLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.login.GuiLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.login.TextLoginHandler;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.CertEntry;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class KeystoreLoginTool extends BaseLoginTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dkeystoreLogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ukeystoreLogin";

	static private Log _logger = LogFactory.getLog(KeystoreLoginTool.class);

	protected KeystoreLoginTool(String description, String usage, boolean isHidden)
	{
		super(description, usage, isHidden);
	}

	public KeystoreLoginTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
	}

	/**
	 * Prompts the user to select an identity from the specified keystore, delegating the selected
	 * credential to the delegatee.
	 * 
	 * If delegatee is null, the credential is delegated to the calling context's current client key
	 * material, in which case it will be self-renewing.
	 * 
	 * @param keystoreInput
	 * @param callContext
	 * @param delegateeIdentity
	 * @return
	 * @throws Throwable
	 */
	protected ArrayList<NuCredential> doKeystoreLogin(InputStream keystoreInput, ICallingContext callContext,
		X509Certificate[] delegateeIdentity) throws Throwable
	{

		ArrayList<NuCredential> retval = new ArrayList<NuCredential>();

		AbstractLoginHandler handler = null;
		if (!useGui() || !GuiUtils.supportsGraphics()) {
			handler = new TextLoginHandler(stdout, stderr, stdin);
		} else {
			handler = new GuiLoginHandler(stdout, stderr, stdin);
		}

		CertEntry certEntry = handler.selectCert(keystoreInput, _storeType, _password, _aliasPatternFlag, _pattern);
		if (certEntry == null) {
			return null;
		}

		// If desired, replace the primary client identity (used for
		// SSL & message signing) with the
		// one specified
		if (_replaceClientToolIdentityFlag) {
			// force new credentials to be created with our preferred lifetime.
			ClientUtils.invalidateCredentials(callContext);

			String msg =
				"Replacing client tool identity with credentials for \"" + certEntry._certChain[0].getSubjectDN().getName()
					+ "\".";
			stdout.println(msg);
			_logger.info(msg);

			KeyAndCertMaterial clientKeyMaterial = new KeyAndCertMaterial(certEntry._certChain, certEntry._privateKey);
			callContext.setActiveKeyAndCertMaterial(clientKeyMaterial);

			// continue on to get a credential also.
		}

		String msg = "Acquiring credentials for \"" + certEntry._certChain[0].getSubjectDN().getName() + "\".";
		stdout.println(msg);
		_logger.info(msg);

		KeyAndCertMaterial clientKeyMaterial =
			ClientUtils.checkAndRenewCredentials(callContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

		if (delegateeIdentity == null) {
			// Delegate the identity assertion to the temporary client identity.
			TrustCredential assertion =
				new TrustCredential(clientKeyMaterial._clientCertChain, IdentityType.CONNECTION, certEntry._certChain,
					IdentityType.USER, new BasicConstraints(System.currentTimeMillis()
						- SecurityConstants.CredentialGoodFromOffset, _credentialValidMillis,
						SecurityConstants.MaxDelegationDepth), RWXCategory.FULL_ACCESS);
			assertion.signAssertion(certEntry._privateKey);
			retval.add(assertion);

		} else {
			// create a static attribute delegated to the specified party
			TrustCredential assertion =
				new TrustCredential(delegateeIdentity, IdentityType.CONNECTION, certEntry._certChain, IdentityType.USER,
					new BasicConstraints(System.currentTimeMillis() - SecurityConstants.CredentialGoodFromOffset,
						_credentialValidMillis, SecurityConstants.MaxDelegationDepth), RWXCategory.FULL_ACCESS);
			assertion.signAssertion(certEntry._privateKey);
			retval.add(assertion);
		}

		return retval;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		if (_storeType == null)
			_storeType = "PKCS12";

		_authnUri = getArgument(0);
		GeniiPath gPath = null;

		if (_authnUri != null)
			gPath = new GeniiPath(_authnUri);

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null)
			callContext = new CallingContextImpl(new ContextType());

		ArrayList<NuCredential> creds = null;

		if (gPath == null) {
			// login to keystore built into the user's OS
			creds = doKeystoreLogin(null, callContext, null);
		} else {
			InputStream in = null;

			try {
				in = gPath.openInputStream();
				creds = doKeystoreLogin(in, callContext, null);
			} finally {
				if (in != null)
					in.close();
			}
		}

		if (creds == null)
			return 0;

		// insert the assertion into the calling context's transient creds
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callContext);
		transientCredentials.addAll(creds);
		ContextManager.storeCurrentContext(callContext);

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 1)
			throw new InvalidToolUsageException();

		if (_durationString != null) {
			try {
				_credentialValidMillis = (long) new Duration(_durationString).as(DurationUnits.Milliseconds);
			} catch (IllegalArgumentException pe) {
				throw new ToolException("Invalid duration string given.", pe);
			}
		}
	}
}
