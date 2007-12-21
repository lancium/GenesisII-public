package edu.virginia.vcgr.genii.client.cmd.tools;

import java.net.*;
import java.io.*;

import javax.xml.namespace.QName;

import java.text.SimpleDateFormat;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernameTokenIdentity;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.X509Identity;
import edu.virginia.vcgr.genii.client.security.SecurityConstants;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.*;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.*;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;

import org.oasis_open.docs.ws_sx.ws_trust._200512.*;
import org.apache.axis.message.MessageElement;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.X509Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.*;

public class GamlLoginTool extends BaseGridTool {

	static public final String PKCS12 = "PKCS12";
	static public final String JKS = "JKS";
	static public final String WINDOWS = "WIN";
;
	static private final String _DESCRIPTION = "Inserts authentication information into the user's context.";
	static private final String _USAGE = ""
			+ "login "
			+ "[--storetype=<PKCS12|JKS|WIN>] "
			+ "[--password=<keystore-password>] " 
			+ "[--alias] "
			+ "[--pattern=<certificate/token pattern>] "
			+ "[--validMillis=<valid milliseconds>] " 
			+ "[<keystore URL>]\n"
			
			+ "login "
			+ "[--validMillis=<valid milliseconds>] " 
			+ "[--username=<username> [--password=<password>] ] "			
			+ "rns:<identity provider path>\n"

			+"login "
			+ "--username=<username>  "
			+ "[--password=<password>]";			

	protected String _password = null;
	protected String _storeType = null;
	protected long _validMillis = GenesisIIConstants.CredentialExpirationMillis;
	protected boolean _aliasPatternFlag = false;
	protected String _username = null;
	protected String _pattern = null;
	protected String _authnUri = null;

	protected GamlLoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}
	
	public GamlLoginTool() {
		super(_DESCRIPTION, _USAGE, false);
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

	public void setValidMillis(String millis) {
		_validMillis = Long.parseLong(millis);
	}
	
	public static SignedAssertion extractAssertion(RequestSecurityTokenResponseType reponseMessage) 
		throws Throwable {
			
		String tokenType = null;
		SignedAssertion responseAssertion = null;
		
		for (MessageElement element : reponseMessage.get_any()) {
			if (element.getName().equals("TokenType")) {
				// process TokenType element
				tokenType = element.getValue();
				
			} else if (element.getName().equals("RequestedSecurityToken")) {
				// process RequestedSecurityToken element
				RequestedSecurityTokenType rstt = null;
				try {
					rstt = (RequestedSecurityTokenType) element.getObjectValue(RequestedSecurityTokenType.class);
				} catch (Exception e) {}
				if (rstt != null) {
					for (MessageElement subElement : rstt.get_any()) {
						if (subElement.getQName().equals(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"))) {
							subElement = subElement.getChildElement(
								new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
							if (subElement != null) {
								subElement = subElement.getChildElement(BinarySecurity.TOKEN_BST);
								if (subElement != null) {
									if (subElement.getAttributeValue("ValueType").equals(SecurityConstants.GAML_TOKEN_TYPE)) {

								        Node text = subElement.getFirstChild();
								        if ((text == null) || (!(text instanceof Text))) {
											throw new Exception("Unknown response token type");
								        }
										String encodedAssertion = ((Text) text).getData();
										responseAssertion = DelegatedAssertion.base64decodeAssertion(encodedAssertion);
									} else {
										throw new Exception("Unknown response token type");
									}
								}
							}
						}
					}
				}
			}
		}
		
		// check requested token type
		if ((tokenType == null) || !tokenType.equals(SecurityConstants.GAML_TOKEN_TYPE)) {
			throw new Exception("Unknown response token type");
		}
		
		return responseAssertion;
	}
	
	
	/**
	 * Calls requestSecurityToken2() on the specified idp.  If delegateAttribute is
	 * non-null, the returned tokens are delegated to that identity (the common-case).
	 */
	public static ArrayList<SignedAssertion> doIdpLogin(EndpointReferenceType idpEpr,
			RenewableClientAttribute delegateAttribute, long validMillis) throws Throwable {

		// assemble the request message
		RequestSecurityTokenType request = new RequestSecurityTokenType();
		ArrayList<MessageElement> elements = new ArrayList<MessageElement>();

		// Add TokenType element
		MessageElement element = new MessageElement(new QName(
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"TokenType"), SecurityConstants.GAML_TOKEN_TYPE);
		element.setType(new QName("http://www.w3.org/2001/XMLSchema",
				"anyURI"));
		elements.add(element);

		// Add RequestType element
		element = new MessageElement(new QName(
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
		if (delegateAttribute != null) {
			MessageElement binaryToken = new MessageElement(
					BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", X509Security.getType());
			binaryToken.addTextNode("");
			BinarySecurity bstToken = new X509Security(binaryToken);
			((X509Security) bstToken).setX509Certificate(delegateAttribute.getDelegateeIdentity()[0]);

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
		
		ArrayList<SignedAssertion> retval = new ArrayList<SignedAssertion>();
		
		if (responses != null) {
			for (RequestSecurityTokenResponseType response : responses) {
				retval.add(extractAssertion(response));
			}
		}

		return retval;
	}

	protected ArrayList<SignedAssertion> doKeystoreLogin(
			InputStream keystoreInput, 
			RenewableClientAttribute delegateAttribute)
			throws Throwable {

		ArrayList<SignedAssertion> retval = new ArrayList<SignedAssertion>();

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
		RenewableIdentityAttribute identityAttr = new RenewableIdentityAttribute(
			new BasicConstraints(
				System.currentTimeMillis() - (1000L * 60 * 15), // 15 minutes ago
				_validMillis, // valid 24 hours
				10),
			new X509Identity(certEntry._certChain));
		RenewableAttributeAssertion identityAssertion = new RenewableAttributeAssertion(
				identityAttr, certEntry._privateKey);

		// get the calling context (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		// Delegate the identity assertion to the temporary client
		// identity
		delegateAttribute.setAssertion(identityAssertion);
		retval.add(new RenewableClientAssertion(delegateAttribute, certEntry._privateKey));

		return retval;
	}

	protected ArrayList<SignedAssertion> delegateToIdentity(
			URI authnUri, 
			RenewableClientAttribute delegateAttribute)
			throws Throwable {

		String protocol = (authnUri == null) ? null : authnUri.getScheme();

		if (authnUri == null) {
			// login to keystore built into the user's OS
			return doKeystoreLogin(null, delegateAttribute);
		}

		if ((protocol == null) || protocol.equals("file")) {
			// log into keystore from a specific file
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(authnUri.getSchemeSpecificPart()));
			try {
				return doKeystoreLogin(fis, delegateAttribute);
			} finally {
				fis.close();
			}
		}

		if (protocol.equals("rns")) {
			RNSPath authnPath = RNSPath.getCurrent().lookup(authnUri.getSchemeSpecificPart(),
					RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType epr = authnPath.getEndpoint();
			TypeInformation type = new TypeInformation(epr);
			if (type.isIDP()) {
				// we're going to use the WS-TRUST token-issue operation
				// to log in to a security tokens service
				return doIdpLogin(epr, delegateAttribute, _validMillis);
			} else if (type.isByteIO()) {
	
				// log into keystore from rns path to keystore file
				BufferedInputStream fis = new BufferedInputStream(new ByteIOInputStream(epr));
				try {
					return doKeystoreLogin(fis, delegateAttribute);
				} finally {
					fis.close();
				}
			}
		}

		throw new IOException("No such protocol handler");
	}

	@Override
	protected int runCommand() throws Throwable {

		_authnUri = getArgument(0);
		URI authnSource = PathUtils.pathToURI(_authnUri);

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		// handle username/token login
		UsernameTokenIdentity utCredential = null;
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
			utCredential = new UsernameTokenIdentity(_username, _password);
			
			TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(callContext);
			transientCredentials._credentials.add(utCredential);

			if (_authnUri == null) { 
				ContextManager.storeCurrentContext(callContext);
				return 0;
			}
		}
		
		
		// create the delegateeAttribute
		RenewableClientAttribute delegateeAttribute = 
			new RenewableClientAttribute(null, callContext);
		
		TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(callContext);
		try {
			// log in
			ArrayList<SignedAssertion> signedAssertions = 
				delegateToIdentity(authnSource, delegateeAttribute);
	
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
	protected void verify() throws ToolException {
		int numArgs = numArguments();
		if (numArgs > 1) {
			throw new InvalidToolUsageException();
		} else if ((numArgs == 0) && 
				(_username == null) && 
				((_storeType == null) || (!_storeType.equals(WINDOWS)))) {
			throw new InvalidToolUsageException();
		}
	}

}