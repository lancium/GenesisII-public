package edu.virginia.vcgr.genii.client.cmd.tools;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.X509Security;
import org.oasis_open.docs.ws_sx.ws_trust._200512.DelegateToType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestTypeOpenEnum;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestedSecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.AttributedDateTime;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.WSSecurityUtils;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.x509authn.X509AuthnPortType;

public class IDPLoginTool extends BaseLoginTool{


	static private final String _DESCRIPTION = "Authenticates against IDP.";
	static private final String _USAGE_RESOURCE = 
		"login [--validDuration=<duration-string>] rns:<identity provider path>";

	protected IDPLoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}

	public IDPLoginTool() {
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
	}

	public static GIICredential extractAssertion(RequestSecurityTokenResponseType reponseMessage) 
	throws Throwable {

		for (MessageElement element : reponseMessage.get_any()) {

			if (element.getName().equals("RequestedSecurityToken")) {
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
	public static ArrayList<GIICredential> doIdpLogin(
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

		// Add RequestType element
		MessageElement element = new MessageElement(new QName(
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
		"RequestType"),
		new RequestTypeOpenEnum(RequestTypeEnum._value1));
		element.setType(RequestTypeOpenEnum.getTypeDesc().getXmlType());
		elements.add(element);

		// Add Lifetime element
		SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		zulu.setTimeZone(TimeZone.getTimeZone("ZULU"));
		element = new MessageElement(
				new QName(
						"http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
				"Lifetime"), 
				new LifetimeType(
						new AttributedDateTime(
								zulu.format(new Date(
										System.currentTimeMillis() - 
										GenesisIIConstants.CredentialGoodFromOffset))), 
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

		ArrayList<GIICredential> retval = new ArrayList<GIICredential>();

		if (responses != null) {
			for (RequestSecurityTokenResponseType response : responses) {
				retval.add(extractAssertion(response));
			}
		}

		return retval;
	}

	@Override
	protected int runCommand() throws Throwable
	{


		_authnUri = getArgument(0);
		URI authnSource = PathUtils.pathToURI(_authnUri);

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}



		TransientCredentials transientCredentials = TransientCredentials
		.getTransientCredentials(callContext);

		// we're going to use the WS-TRUST token-issue operation
		// to log in to a security tokens service
		KeyAndCertMaterial clientKeyMaterial = 
			ClientUtils.checkAndRenewCredentials(callContext, 
					new Date(), new SecurityUpdateResults());

		RNSPath authnPath = callContext.getCurrentPath().lookup(
				authnSource.getSchemeSpecificPart(),
				RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType epr = authnPath.getEndpoint();


		// log in
		ArrayList<GIICredential> signedAssertions = doIdpLogin(epr, _validMillis, clientKeyMaterial._clientCertChain);
		if (signedAssertions == null) {
			return 0;
		}	

		// insert the assertion into the calling context's transient creds
		transientCredentials._credentials.addAll(signedAssertions);
		ContextManager.storeCurrentContext(callContext);	
		return 0;
	}


	@Override
	protected void verify() throws ToolException 
	{
		int numArgs = numArguments();
		if (numArgs != 1) 
			throw new InvalidToolUsageException();

		if (_durationString != null)
		{
			try
			{
				_validMillis = (long)new Duration(
						_durationString).as(DurationUnits.Milliseconds);
			}
			catch (IllegalArgumentException pe)
			{
				throw new ToolException("Invalid duration string given.", pe);
			}
		}
	}


}
