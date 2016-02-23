package edu.virginia.vcgr.genii.security.axis;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
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
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.nio.SelectChannelEndPoint;
import org.eclipse.jetty.io.nio.SslConnection;
import org.eclipse.jetty.io.nio.SslConnection.SslEndPoint;
import org.eclipse.jetty.server.Request;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.CredentialCache;
import edu.virginia.vcgr.genii.security.credentials.ServerSideStreamliningCredentialCache;
import edu.virginia.vcgr.genii.security.credentials.TimedOutCredentialsCachePerSession;

/**
 * This Axis intercepter/handler class is used for retrieving the SAML credentials wallet a client delegates to a resource when issuing an
 * RPC. Note that this class is populating the working context with the retrieved credentials wallet. So this invoker must be invoked after we
 * have already processed the working context from the SOAP message context.
 * 
 * @author myanhaona
 * @author ckoeritz
 */
public class CredentialsWalletRetriever extends BasicHandler
{
	private static final long serialVersionUID = 1L;

	static private Log _logger = LogFactory.getLog(CredentialsWalletRetriever.class);

	// hmmm: move this method into a more sensible location; must know axis types.
	/**
	 * looks into the low-level jetty support in order to find the client's TLS certificate.
	 */
	public static X509Certificate[] getClientTLSCert(MessageContext messageContext) throws AxisFault
	{
		// Grab the client-hello authenticated SSL cert-chain (if there was one)
		org.eclipse.jetty.server.Request req = (Request) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

		Object transport = req.getConnection().getEndPoint().getTransport();
		X509Certificate[] clientSslCertChain = null;
		if (transport instanceof SelectChannelEndPoint) {
			try {
				if (((SelectChannelEndPoint) transport).getConnection() instanceof SslConnection) {
					if (req.getConnection().getEndPoint() instanceof EndPoint) {
						EndPoint ep = req.getConnection().getEndPoint();
						SSLSession session = ((SslEndPoint) ep).getSslEngine().getSession();
						if (session != null) {
							Certificate[] peerCerts = null;
							try {
								peerCerts = session.getPeerCertificates();
							} catch (SSLPeerUnverifiedException e) {
								_logger.error("Peer unverified while attempting to extract peer certificates.", e);
							}

							clientSslCertChain = new X509Certificate[peerCerts.length];
							int i = 0;
							for (Certificate c : peerCerts) {
								if (c instanceof X509Certificate) {
									clientSslCertChain[i++] = (X509Certificate) c;
								} else {
									throw new AxisFault("found wrong type of certificates for peer!");
								}
							}
						}

						if (_logger.isTraceEnabled())
							_logger.debug("incoming client ssl cert chain is: " + clientSslCertChain[0].getSubjectDN());
					}

					if (clientSslCertChain == null) {
						throw new SSLPeerUnverifiedException("failed to find client SSL cert chain; jetty would not fess up.");
					}
				}
			} catch (SSLPeerUnverifiedException unverified) {
			}
		}
		return clientSslCertChain;
	}

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

		AxisCredentialWallet wallet = null;

		SOAPHeaderElement referencesHeader = null;

		// first try to locate the references header, which is optional except in credential streamlining mode.
		Iterator<?> iter1 = header.examineAllHeaderElements();
		while (iter1.hasNext()) {
			SOAPHeaderElement headerElement = (SOAPHeaderElement) iter1.next();
			QName headerName = new QName(headerElement.getNamespaceURI(), headerElement.getLocalName());
			if (headerName.equals(GenesisIIConstants.REFERENCED_SAML_CREDENTIALS_QNAME)) {
				referencesHeader = headerElement;
			}
		}

		TimedOutCredentialsCachePerSession clientCreds = null;
		X509Certificate x509[] = getClientTLSCert(messageContext);
		if (CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED && (x509 != null)) {
			clientCreds =
				ServerSideStreamliningCredentialCache.getServerSideStreamliningCredentialCache().get(x509[0].getSubjectDN().toString());
		}

		// now look for the crucial header with our saml assertions.
		boolean foundIt = false;
		Iterator<?> iter = header.examineAllHeaderElements();
		while (iter.hasNext()) {
			SOAPHeaderElement headerElement = (SOAPHeaderElement) iter.next();
			QName headerName = new QName(headerElement.getNamespaceURI(), headerElement.getLocalName());
			if (headerName.equals(GenesisIIConstants.DELEGATED_SAML_ASSERTIONS_QNAME)) {
				try {
					X509Certificate[] clientSslCertChain = CredentialsWalletRetriever.getClientTLSCert(messageContext);
					wallet = new AxisCredentialWallet((org.apache.axis.message.SOAPHeaderElement) headerElement,
						(org.apache.axis.message.SOAPHeaderElement) referencesHeader, clientCreds, clientSslCertChain[0]);
					if (_logger.isTraceEnabled()) {
						_logger.trace("retrieved credential wallet:\n" + wallet.getRealCreds().describe(VerbosityLevel.HIGH));
					}
					foundIt = true;
					break;
				} catch (Throwable t) {
					String msg = "could not grab all credentials expected from soap header: "
						+ ((t.getMessage() != null) ? t.getMessage() : "empty exception message");
					if (_logger.isDebugEnabled())
						_logger.debug(msg);
					throw new AxisFault(msg, t);
				}
			}
		}

		// make sure we don't give them a dead wallet.
		if (wallet == null) {
			wallet = new AxisCredentialWallet();
		}

		if (CredentialCache.SERVER_CREDENTIAL_STREAMLINING_ENABLED) {
			// show all the credentials that are referenced but possibly not sent in header.
			List<String> referencedCreds = wallet.getLastAttachmentReferences();
			if (referencedCreds != null) {
				StringBuilder sb = new StringBuilder();
				for (String id : referencedCreds) {
					sb.append(id);
					sb.append(" ");
				}
				if (_logger.isTraceEnabled())
					_logger.debug("creds referenced were: " + sb.toString());
			}
		}

		if (foundIt && wallet.getRealCreds().getCredentials().isEmpty()) {
			if (_logger.isTraceEnabled())
				_logger.trace("did not retrieve any credentials into wallet, although we saw our credentials soap header.");
		}

		WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();

		Object oldcreds = workingContext.getProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME);
		if (oldcreds != null) {
			_logger.error("failure; overwriting existing value for saml wallet in working context");
		}

		workingContext.setProperty(SAMLConstants.SAML_CREDENTIALS_WORKING_CONTEXT_CREDS_PROPERTY_NAME, wallet.getRealCreds());

		retrieveSSLCertificate(messageContext, workingContext);
	}

	/**
	 * A caller's SSL certificate is not a part of the delegated SAML credentials wallet. Nevertheless, we retrieve it and store it for the
	 * lifetime of an RPC because some of the access control rule may be directly applicable to the SSL certificate in use.
	 */
	private void retrieveSSLCertificate(MessageContext messageContext, WorkingContext workingContext)
	{
		Request request = (Request) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		Object transport = request.getConnection().getEndPoint().getTransport();
		if (transport instanceof SSLSocket) {
			try {
				X509Certificate[] clientSSLCertificate = (X509Certificate[]) ((SSLSocket) transport).getSession().getPeerCertificates();
				if (clientSSLCertificate != null) {
					workingContext.setProperty(SAMLConstants.SAML_CLIENT_SSL_CERTIFICATE_PROPERTY_NAME, clientSSLCertificate);
				}
			} catch (SSLPeerUnverifiedException unverified) {
				_logger.error("failed to grab SSL certificate of the client despite using SSL socket.");
			}
		}
	}
}
