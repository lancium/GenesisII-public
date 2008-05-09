package edu.virginia.vcgr.genii.client.cmd.tools;

import java.net.*;
import java.io.*;

import javax.xml.namespace.QName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.X509Identity;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.GenericQuestionWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.YesNoCancelType;
import edu.virginia.vcgr.genii.client.utils.dialog.YesNoWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.text.TextWidgetProvider;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.*;
import edu.virginia.vcgr.genii.container.sysinfo.SupportedOperatingSystems;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.*;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;

import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.apache.axis.message.MessageElement;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.X509Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.*;

import java.security.cert.X509Certificate;

public class GamlLoginTool extends BaseGridTool {

	static public final String PKCS12 = "PKCS12";
	static public final String JKS = "JKS";
	static public final String WINDOWS = "WIN";
;
	static private final String _DESCRIPTION = "Inserts authentication information into the user's context.";
	static private final String _USAGE_RESOURCE = 
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/login-usage.txt";
	protected String _password = null;
	protected String _storeType = null;
	protected String _durationString = null;
	protected long _validMillis = GenesisIIConstants.CredentialExpirationMillis;
	protected boolean _aliasPatternFlag = false;
	protected String _username = null;
	protected String _pattern = null;
	protected String _authnUri = null;

	protected GamlLoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}
	
	public GamlLoginTool() {
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}

	public void setStoretype(String storeType) {
		_storeType = storeType;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public void setAlias() {
		_aliasPatternFlag = true;
	}

	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	public void setValidDuration(String durationString) 
		throws ParseException
	{
		_durationString = durationString;
	}
	
	public static GamlCredential extractAssertion(RequestSecurityTokenResponseType reponseMessage) 
		throws Throwable {
			
		for (MessageElement element : reponseMessage.get_any()) {
			if (element.getName().equals("TokenType")) {
				// process TokenType element
				// String tokenType = element.getValue();
				
			} else if (element.getName().equals("RequestedSecurityToken")) {
				// process RequestedSecurityToken element
				RequestedSecurityTokenType rstt = null;
				try {
					rstt = (RequestedSecurityTokenType) element.getObjectValue(RequestedSecurityTokenType.class);
				} catch (Exception e) {}
				if (rstt != null) {
					for (MessageElement subElement : rstt.get_any()) {
						try {
							return WSSecurityUtils.decodeTokenElement(subElement);							
						} catch (Exception e) {}
					}
				}
			}
		}
		
		throw new Exception("Unknown response token type");
	}
	
	
	/**
	 * Calls requestSecurityToken2() on the specified idp.  If delegateAttribute is
	 * non-null, the returned tokens are delegated to that identity (the common-case).
	 */
	public static ArrayList<GamlCredential> doIdpLogin(
			EndpointReferenceType idpEpr,
			long validMillis,
			X509Certificate[] delegateeIdentity) throws Throwable {

		// get the calling context (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
			ContextManager.storeCurrentContext(callContext);
		}
		
		// assemble the request message
		RequestSecurityTokenType request = new RequestSecurityTokenType();
		ArrayList<MessageElement> elements = new ArrayList<MessageElement>();

/* Don't care, necessarily		
		// Add TokenType element
		MessageElement element = new MessageElement(new QName(
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"TokenType"), WSSecurityUtils.GAML_TOKEN_TYPE);
		element.setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));
		elements.add(element);
*/		

		// Add RequestType element
		MessageElement element = new MessageElement(new QName(
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"RequestType"),
				new RequestTypeOpenEnum(RequestTypeEnum._value1));
		element.setType(RequestTypeOpenEnum.getTypeDesc().getXmlType());
		elements.add(element);

		// Add Lifetime element
	    SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
	    element = new MessageElement(
				new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
					"Lifetime"), 
				new LifetimeType(
					new AttributedDateTime(
						zulu.format(new Date())), 
					new AttributedDateTime(
						zulu.format(new Date(System.currentTimeMillis() + validMillis)))));
		element.setType(LifetimeType.getTypeDesc().getXmlType());
		elements.add(element);

		// Add DelegateTo
		if (delegateeIdentity != null) {
			MessageElement binaryToken = new MessageElement(
					BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", X509Security.getType());
			binaryToken.addTextNode("");
			BinarySecurity bstToken = new X509Security(binaryToken);
			((X509Security) bstToken).setX509Certificate(delegateeIdentity[0]);

			MessageElement embedded = new MessageElement(new QName(
					org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
			embedded.addChild(binaryToken);
	
			MessageElement wseTokenRef = new MessageElement(new QName(
					org.apache.ws.security.WSConstants.WSSE11_NS,
					"SecurityTokenReference"));
			wseTokenRef.addChild(embedded);
	
			element = new MessageElement(new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
					"DelegateTo"), new DelegateToType(
					new MessageElement[] { wseTokenRef }));
			element.setType(DelegateToType.getTypeDesc().getXmlType());
			elements.add(element);
		}

		MessageElement[] elemArray = new MessageElement[elements.size()];
		request.set_any(elements.toArray(elemArray));
		
		// create a proxy to the remote idp and invoke it
		X509AuthnPortType idp = ClientUtils.createProxy(
				X509AuthnPortType.class, idpEpr);
		RequestSecurityTokenResponseType[] responses = idp
				.requestSecurityToken2(request);
		
		ArrayList<GamlCredential> retval = new ArrayList<GamlCredential>();
		
		if (responses != null) {
			for (RequestSecurityTokenResponseType response : responses) {
				retval.add(extractAssertion(response));
			}
		}

		return retval;
	}

	protected ArrayList<GamlCredential> doKeystoreLogin(
			InputStream keystoreInput, 
			ICallingContext callContext,
			X509Certificate[] delegateeIdentity) throws Throwable {

		ArrayList<GamlCredential> retval = new ArrayList<GamlCredential>();

		AbstractGamlLoginHandler handler = null;
		if (!useGui() || !GuiUtils.supportsGraphics()) {
			handler = new TextGamlLoginHandler(stdout, stderr, stdin);
		} else {
			handler = new GuiGamlLoginHandler(stdout, stderr, stdin);
		}

		CertEntry certEntry = handler.selectCert(keystoreInput, _storeType,
				_password, _aliasPatternFlag, _pattern);
		if (certEntry == null) {
			return null;
		}

		stdout.println("Acquiring credentials for \""
				+ certEntry._certChain[0].getSubjectDN().getName() + "\".");

		// Create identity assertion
		X509Identity identityAssertion = 
			new X509Identity(certEntry._certChain);
		
		// create the delegateeAttribute
		if (delegateeIdentity == null) {
			
			// create a renewable attribute delegated to the calling context
			RenewableClientAttribute delegateeAttribute = new RenewableClientAttribute(
				new BasicConstraints(
					System.currentTimeMillis() - (1000L * 60 * 15), 	// 15 minutes ago
					_validMillis, 										// default 24 hours valid
					10),												// delegation depth 10 
				identityAssertion,
				callContext);
			
			// Delegate the identity assertion to the temporary client
			// identity
			retval.add(new RenewableClientAssertion(delegateeAttribute, certEntry._privateKey));

		} else {
			
			// create a static attribute delegated to the specified party 
			DelegatedAttribute delegateeAttribute = new DelegatedAttribute(
				new BasicConstraints(
					System.currentTimeMillis() - (1000L * 60 * 15), 	// 15 minutes ago
					_validMillis, 										// default 24 hours valid
					10),												// delegation depth 10 
				identityAssertion,
				delegateeIdentity);
			
			retval.add(new DelegatedAssertion(delegateeAttribute, certEntry._privateKey));
		}		

		return retval;
	}

	protected ArrayList<GamlCredential> delegateToIdentity(
			URI authnUri, ICallingContext callingContext) throws Throwable {
		
		return delegateToIdentity(authnUri, callingContext, null);
	}

	protected ArrayList<GamlCredential> delegateToIdentity(
		URI authnUri, ICallingContext callingContext, X509Certificate[] delegatee) throws Throwable {

		String protocol = (authnUri == null) ? null : authnUri.getScheme();

		if (authnUri == null) {
			// login to keystore built into the user's OS
			return doKeystoreLogin(null, callingContext, delegatee);
		}

		if ((protocol == null) || protocol.equals("file")) {
			// log into keystore from a specific file
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(authnUri.getSchemeSpecificPart()));
			try {
				return doKeystoreLogin(fis, callingContext, delegatee);
			} finally {
				fis.close();
			}
		}
		
		
		if (protocol.equals("rns")) {
			RNSPath authnPath = callingContext.getCurrentPath().lookup(authnUri.getSchemeSpecificPart(),
					RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType epr = authnPath.getEndpoint();
			TypeInformation type = new TypeInformation(epr);
			if (type.isIDP()) {
				// we're going to use the WS-TRUST token-issue operation
				// to log in to a security tokens service
				KeyAndCertMaterial clientKeyMaterial = 
					ClientUtils.checkAndRenewCredentials(callingContext);
				return doIdpLogin(
						epr, 
						_validMillis, 
						(delegatee != null) ? delegatee : clientKeyMaterial._clientCertChain);
			} else if (type.isByteIO()) {
	
				// log into keystore from rns path to keystore file
				InputStream in = ByteIOStreamFactory.createInputStream(epr);
				try {
					return doKeystoreLogin(in, callingContext, delegatee);
				} finally {
					in.close();
				}
			}
			throw new IOException("Not a supported identity provider");
		}

		throw new IOException("No such protocol handler");
	}

	@Override
	protected int runCommand() throws Throwable
	{
		if (numArguments() == 0 
			&& _username == null 
			&& _storeType == null
			&& _password == null
			&& _pattern == null)
		{
			if (queryUserForDefaultOptions())
				return 0;
		}
		
		_authnUri = getArgument(0);
		URI authnSource = PathUtils.pathToURI(_authnUri);

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		// handle username/token login
		UsernamePasswordIdentity utCredential = null;
		if (_username != null) {
			if (_password == null) {
				AbstractGamlLoginHandler handler = null;
				if (!useGui() || !GuiUtils.supportsGraphics()) {
					handler = new TextGamlLoginHandler(stdout, stderr, stdin);
				} else {
					handler = new GuiGamlLoginHandler(stdout, stderr, stdin);
				}
				_password = new String(handler.getPassword(
						"Username/Password Login", 
						"Password for " + _username + ": "));
			}
			utCredential = new UsernamePasswordIdentity(_username, _password);
			
			TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(callContext);
			transientCredentials._credentials.add(utCredential);

			if (_authnUri == null) { 
				ContextManager.storeCurrentContext(callContext);
				return 0;
			}
		}
		
		TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(callContext);
		try {
			// log in
			ArrayList<GamlCredential> signedAssertions = 
				delegateToIdentity(authnSource, callContext);
	
			if (signedAssertions == null) {
				return 0;
			}
			
			// insert the assertion into the calling context's transient creds
			transientCredentials._credentials.addAll(signedAssertions);
		} finally {
	
			if (utCredential != null) {
				// the UT credential was used only to log into the IDP, remove it
				transientCredentials._credentials.remove(utCredential);
			}
		}
		
		ContextManager.storeCurrentContext(callContext);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException 
	{
		int numArgs = numArguments();
		if (numArgs > 1) 
			throw new InvalidToolUsageException();
		
		if (_durationString != null)
		{
			try
			{
				_validMillis = Duration.parse(_durationString).getMilliseconds();
			}
			catch (ParseException pe)
			{
				throw new ToolException("Invalid duration string given.", pe);
			}
		}
	}
	
	/**
	 * This method is called by login when no parameters whatsover are given.
	 * It will ask the user for a list of "default" options to select from and
	 * then log the user in using those by filling in the correct tool
	 * parameter values.  The intent is that this function essentially fills
	 * in parameters that would normally have been given on the command line
	 * had the user typed them in.
	 * 
	 * @return A boolean value indicating whether the login was cancelled 
	 * (true), or not.
	 */
	private boolean queryUserForDefaultOptions() throws DialogException, IOException
	{
		boolean isWindows = SupportedOperatingSystems.current().equals(
			SupportedOperatingSystems.WINDOWS);
		
		TextWidgetProvider twp = new TextWidgetProvider(stdout, stderr, stdin);
		
		if (isWindows)
		{
			YesNoWidget widget = twp.createYesNoDialog("Use Windows Certificate Store");
			widget.includeCancel(true);
			widget.setDefault(YesNoCancelType.Yes);
			widget.setDetailedHelp(new FileResource(
				"edu/virginia/vcgr/genii/client/cmd/tools/resources/use-windows-cert-help.txt"));
			widget.setPrompt(
				"Would you like to log in using a windows certificate?");
			widget.showWidget();
			YesNoCancelType answer = widget.getAnswer();
			
			if (answer == YesNoCancelType.Yes)
			{
				_storeType = "WIN";
				return false;
			} else if (answer == YesNoCancelType.Cancel)
				return true;
		}
		
		GenericQuestionWidget certPathWidget = twp.createGenericQuestionDialog(
			"Certificate URI");
		certPathWidget.setDetailedHelp(new FileResource(
			"edu/virginia/vcgr/genii/client/cmd/tools/resources/cert-uri-help.txt"));
		certPathWidget.setPrompt("Please enter the URI for your certificate store?");
		certPathWidget.showWidget();
		String path = certPathWidget.getAnswer();
		
		getArguments().clear();
		getArguments().add(path);
		
		if (path.startsWith("rns:"))
		{
			// It's an RNS path, assume its an IDP we are looking for.
			GenericQuestionWidget usernameWidget = twp.createGenericQuestionDialog("IDP Username");
			usernameWidget.setPrompt("Please enter the IDP service instance username:");
			usernameWidget.showWidget();
			String username = usernameWidget.getAnswer();
			
			_username = username;
			return false;
		} else
		{
			// It wasn't an RNS path, so we'll assume it's a PKCS12 store.
			return false;
		}
	}
}