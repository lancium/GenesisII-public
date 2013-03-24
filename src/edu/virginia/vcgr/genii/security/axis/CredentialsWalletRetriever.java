package edu.virginia.vcgr.genii.security.axis;

import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Request;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.VerbosityLevel;

/**
 * This Axis intercepter/handler class is used for retrieving the SAML credentials wallet a client
 * delegates to a resource when issuing an RPC. Note that this class is populating the working
 * context with the retrieved credentials wallet. So this invoker must be invoked after we have
 * already processed the working context from the SOAP message context.
 * 
 * @author myanhaona
 * @author ckoeritz
 */
public class CredentialsWalletRetriever extends BasicHandler
{
	private static final long serialVersionUID = 1L;

	static private Log _logger = LogFactory.getLog(CredentialsWalletRetriever.class);

	@Override
	public void invoke(MessageContext messageContext) throws AxisFault
	{
		SOAPMessage m = messageContext.getMessage();
		SOAPHeader header;
		try {
			header = m.getSOAPHeader();
		} catch (SOAPException se) {
			_logger.error("failed to get soap header from message");
			throw new AxisFault(se.getLocalizedMessage(), se);
		}

		AxisSAMLCredentials wallet = null;
		Iterator<?> iter = header.examineAllHeaderElements();

		boolean foundIt = false;
		while (iter.hasNext()) {
			SOAPHeaderElement headerElement = (SOAPHeaderElement) iter.next();
			QName headerName = new QName(headerElement.getNamespaceURI(), headerElement.getLocalName());
			if (headerName.equals(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME)) {
				wallet = new AxisSAMLCredentials((org.apache.axis.message.SOAPHeaderElement) headerElement);
				if (_logger.isTraceEnabled()) {
					_logger.trace("retrieved credential wallet:");
					_logger.trace(wallet.getRealCreds().describe(VerbosityLevel.HIGH));
				}
				foundIt = true;
				break;
			}
		}

		if (foundIt && wallet.getRealCreds().getCredentials().isEmpty()) {
			_logger.error("failed to retrieve any credentials into wallet, although we saw our credentials soap header!");
		}

		WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();

		Object oldcreds = workingContext.getProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME);
		if (oldcreds != null) {
			_logger.error("failure; overwriting existing value for saml wallet in working context");
		}

		// make sure we don't give them a dead wallet.
		if (wallet == null)
			wallet = new AxisSAMLCredentials();

		workingContext.setProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME, wallet.getRealCreds());

		retrieveSSLCertificate(messageContext, workingContext);
	}

	/**
	 * A caller's SSL certificate is not a part of the delegated SAML credentials wallet.
	 * Nevertheless, we retrieve it and store it for the lifetime of an RPC because some of the
	 * access control rule may be directly applicable to the SSL certificate in use.
	 */
	private void retrieveSSLCertificate(MessageContext messageContext, WorkingContext workingContext)
	{

		Request request = (Request) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		Object transport = request.getConnection().getEndPoint().getTransport();
		if (transport instanceof SSLSocket) {
			try {
				X509Certificate[] clientSSLCertificate = (X509Certificate[]) ((SSLSocket) transport).getSession()
					.getPeerCertificates();
				if (clientSSLCertificate != null) {
					workingContext.setProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME, clientSSLCertificate);
				}
			} catch (SSLPeerUnverifiedException unverified) {
				_logger.error("failed to grab SSL certificate of the client despite using SSL socket.");
			}
		}
	}
}
