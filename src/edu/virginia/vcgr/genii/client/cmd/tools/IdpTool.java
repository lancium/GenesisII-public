package edu.virginia.vcgr.genii.client.cmd.tools;

import java.security.cert.X509Certificate;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool.CertEntry;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.*;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rns.*;

public class IdpTool extends BaseGridTool {
	static private final String _DESCRIPTION = 
		"Creates a proxy authentication object to delegate an X.509 identity";
	static private final String _USAGE = 
		"idp [--storefile=<keystore-file>] " + 
		    "[--storetype=<PKCS12|JKS>] " + 
			"[--storepassword=<keystore-password>] " + 
			"[--alias] " + 
			"[--pattern=<certificate-pattern>] " +
			"<idp serivce path> " + 
			"<username> " + 
			"<password>";

	private String _storeFile = null;
	private String _storePassword = null;
	private String _storeType = null;
	private boolean _aliasPatternFlag = false;
	private String _pattern = null;

	public IdpTool() {
		super(_DESCRIPTION, _USAGE, false);
	}

	public void setStoreType(String storeType) {
		_storeType = storeType;
	}

	public void setStorePassword(String password) {
		_storePassword = password;
	}

	public void setStoreFile(String storeFile) {
		_storeFile = storeFile;
	}

	public void setAlias() {
		_aliasPatternFlag = true;
	}

	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	@Override
	protected int runCommand() throws Throwable {

		String idpServiceRelPath = this.getArgument(0);
		String username = this.getArgument(1);
		String password = this.getArgument(2);

		// get rns path to idp service
		RNSPath idpService = RNSPath.getCurrent().lookup(idpServiceRelPath,
				RNSPathQueryFlags.MUST_EXIST);

		// get the identity of the idp service
		X509Certificate[] idpCertChain = EPRUtils.extractCertChain(idpService
				.getEndpoint());
		if (idpCertChain == null) {
			throw new RNSException("Entry \"" + idpServiceRelPath
					+ "\" is not an IDP service.");
		}

		AbstractGamlLoginHandler handler = null;
		if (!useGui() || !GuiUtils.supportsGraphics())
			handler = new TextGamlLoginHandler(stdout, stderr, stdin);
		else
			handler = new GuiGamlLoginHandler(stdout, stderr, stdin);

		CertEntry certEntry = handler.selectCert(_storeFile, _storeType,
				_storePassword, _aliasPatternFlag, _pattern);
		if (certEntry == null)
			return 0;

		stdout.println("Creating idp identity for \""
				+ certEntry._certChain[0].getSubjectDN().getName() + "\".");

		// Create identity assertion
		IdentityAttribute identityAttr = new IdentityAttribute(System
				.currentTimeMillis()
				- (1000L * 60 * 15), // 15 minutes ago
				1000 * 60 * 60 * 24 * 180, // valid 180 days
				10, new X509Identity(certEntry._certChain));
		SignedAttributeAssertion identityAssertion = new SignedAttributeAssertion(
				identityAttr, certEntry._privateKey);

		// Delegate the identity assertion to the idp service
		DelegatedAttribute delegatedAttr = new DelegatedAttribute(
				identityAssertion, idpCertChain);
		DelegatedAssertion delegatedAssertion = new DelegatedAssertion(
				delegatedAttr, certEntry._privateKey);

		// serialize the delegatedAssertion and put into construction params
		String encodedAssertion = DelegatedAssertion
				.base64encodeAssertion(delegatedAssertion);
		MessageElement delegatedIdentParm = new MessageElement(
				SecurityConstants.IDP_DELEGATED_IDENITY_QNAME, encodedAssertion);
		MessageElement usernameParm = new MessageElement(
				SecurityConstants.IDP_USERNAME_QNAME, username);
		MessageElement passwordParm = new MessageElement(
				SecurityConstants.IDP_PASSWORD_QNAME, username);
		
		MessageElement[] constructionParms = 
			new MessageElement[] { delegatedIdentParm, usernameParm, passwordParm };

		// create the new idp resource and link it into context space
		CreateResourceTool.createInstance(
				idpService.getEndpoint(),
				null,						// no link needed 
				constructionParms);

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs != 3)
			throw new InvalidToolUsageException();
		
	}

}