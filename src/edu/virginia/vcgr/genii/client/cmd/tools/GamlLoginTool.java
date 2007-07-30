package edu.virginia.vcgr.genii.client.cmd.tools;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.RenewableAttributeAssertion;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.RenewableClientAssertion;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.RenewableClientAttribute;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.RenewableIdentityAttribute;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.X509Identity;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.AbstractGamlLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.GuiGamlLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.TextGamlLoginHandler;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class GamlLoginTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Inserts GAML authentication information into the user's context.";
	static private final String _USAGE =
		"login [--file=<keystore-file>] [--storetype=<PKCS12|JKS>] [--password=<keystore-password>] [<certificate-pattern>]";
	
	private String _keystoreFile = null;
	private String _password = null;
	private String _storeType = null;
	
	public GamlLoginTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setStoretype(String storeType)
	{
		_storeType = storeType;
	}
	
	public void setFile(String keystoreFile)
	{
		_keystoreFile = keystoreFile;
	}
	
	public void setPassword(String password)
	{
		_password = password;
	}
		
	@Override
	protected int runCommand() throws Throwable
	{
		AbstractGamlLoginHandler handler = null;
		if (!useGui() || !GuiUtils.supportsGraphics())
			handler = new TextGamlLoginHandler(stdout, stderr, stdin);
		else
			handler = new GuiGamlLoginHandler(stdout, stderr, stdin);
		
		CertEntry certEntry = handler.selectCert(
			_keystoreFile, _storeType, _password, getArgument(0));
		if (certEntry == null)
			return 0;
		
		stdout.println("Logging in as \"" + 
			certEntry._certChain[0].getSubjectDN().getName() + "\".");
		
		// Create identitiy assertion
		RenewableIdentityAttribute identityAttr = new RenewableIdentityAttribute(
			System.currentTimeMillis() - (1000L * 60 * 15), // 15 minutes ago
			GenesisIIConstants.CredentialExpirationMillis,	// valid 24 hours
			10,
			new X509Identity(certEntry._certChain));
		RenewableAttributeAssertion identityAssertion =
			new RenewableAttributeAssertion(identityAttr, certEntry._privateKey);
		
		// get the calling context (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null)
			callContext = new CallingContextImpl(new ContextType());
		
		// Delegate the identity assertion to the temporary client
		// identity
		RenewableClientAttribute delegatedAttr = new RenewableClientAttribute(
			identityAssertion, callContext);
		RenewableClientAssertion delegatedAssertion = new RenewableClientAssertion(
			delegatedAttr, certEntry._privateKey);
		
		// insert the assertion into the calling context's transient creds
		TransientCredentials transientCredentials =
			TransientCredentials.getTransientCredentials(callContext);
		transientCredentials._credentials.add(delegatedAssertion);
		
		ContextManager.storeCurrentContext(callContext);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 1)
			throw new InvalidToolUsageException();
	}
	
	static public class CertEntry
	{
		public X509Certificate []_certChain;
		public String _alias;
		public PrivateKey _privateKey;
		public KeyStore _keyStore;
		public String _friendlyName;
		
		public CertEntry(X509Certificate []certChain, 
			String alias, PrivateKey privateKey, String friendlyName)
		{
			_certChain = certChain;
			_alias = alias;
			_privateKey = privateKey;
			_friendlyName = friendlyName;
			
			if (_friendlyName == null)
				_friendlyName = _certChain[0].getSubjectDN().getName();
		}
		
		public CertEntry(Certificate []certChain, 
			String alias, KeyStore keyStore)
		{
			if (certChain != null)
			{
				_certChain = new X509Certificate[certChain.length];
				for (int i = 0; i < certChain.length; i++)
				{
					_certChain[i] = (X509Certificate)certChain[i];
				}
			}
			
			_alias = alias;
			_keyStore = keyStore;
			
			_friendlyName = alias;
		}
		
		public String toString()
		{
			return _friendlyName;
		}
	}
}