package edu.virginia.vcgr.genii.security.axis;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.message.token.X509Security;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.security.GIIBouncyCrypto;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

/**
 * Operations for converting popular token types to/from MessageElements
 * 
 * @author dmerrill
 */
public class WSSecurityUtils
{
	public static final String PKCS7_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#PKCS7";
	public static final String X509v3_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";
	public static final String X509PKIPathv1_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

	public static final String PASSWORD_DIGEST_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest";
	public static final String PASSWORD_TEXT_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
	public static final String USERNAME_TOKEN_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#UsernameToken";

	static public String getNameTokenFromUTSecTokenRef(MessageElement wseTokenRef) throws GeneralSecurityException
	{
		try {
			UsernameToken bstToken = new UsernameToken(wseTokenRef);
			return bstToken.getName();
		} catch (WSSecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public String getPasswordTokenFromUTSecTokenRef(MessageElement wseTokenRef) throws GeneralSecurityException
	{
		try {
			UsernameToken bstToken = new UsernameToken(wseTokenRef);
			return bstToken.getPassword();
		} catch (WSSecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public MessageElement makeUTSecTokenRef(String username, String password) throws GeneralSecurityException
	{
		try {
			MessageElement userElem = new MessageElement(WSConstants.WSSE_NS, "Username");
			userElem.addTextNode(username);
			MessageElement passElem = new MessageElement(WSConstants.WSSE_NS, "Password");
			passElem.addTextNode(password);
			MessageElement token = new MessageElement(UsernameToken.TOKEN);
			token.addChild(userElem);
			token.addChild(passElem);

			return token;

		} catch (Exception e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	// returns true if the element seems to contain a recognized security token reference.
	static public boolean matchesSecurityToken(MessageElement element)
	{
		return element.getQName().equals(GenesisIIConstants.WSSE11_NS_SECURITY_QNAME)
			|| element.getQName().equals(GenesisIIConstants.INTERMEDIATE_WSE_NS_SECURITY_QNAME);
	}

	// finds a child element in the security token, regardless of namespace.
	static public MessageElement acquireChildSecurityElement(MessageElement element, String elementName)
	{
		MessageElement elem = element.getChildElement(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, elementName));
		if (elem == null)
			elem = element.getChildElement(new QName(GenesisIIConstants.INTERMEDIATE_WSE_NS, elementName));
		return elem;
	}

	static public X509Certificate[] getChainFromPkiPathSecTokenRef(MessageElement wseTokenRef) throws GeneralSecurityException
	{
		MessageElement element = acquireChildSecurityElement(wseTokenRef, "Embedded");
		if (element != null) {
			element = element.getChildElement(BinarySecurity.TOKEN_BST);
			if (element != null) {
				try {
					PKIPathSecurity bstToken = new PKIPathSecurity(element);
					return bstToken.getX509Certificates(false, new GIIBouncyCrypto());
				} catch (GenesisIISecurityException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (WSSecurityException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (IOException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (CredentialException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				}
			}
		}

		throw new GeneralSecurityException("Message element does not contain a PKIPath certificate chain");
	}

	static public MessageElement makePkiPathSecTokenRef(X509Certificate[] certChain) throws GeneralSecurityException
	{
		return makePkiPathSecTokenRef(certChain, null);
	}

	static public MessageElement makePkiPathSecTokenRef(X509Certificate[] certChain, String wsuId)
		throws GeneralSecurityException
	{
		try {
			MessageElement binaryToken = new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", PKIPathSecurity.getType());
			if (wsuId != null) {
				binaryToken.setAttribute(WSConstants.WSU_NS, "Id", wsuId);
			}
			binaryToken.addTextNode("");
			BinarySecurity bstToken = new PKIPathSecurity(binaryToken);
			((PKIPathSecurity) bstToken).setX509Certificates(certChain, false, new GIIBouncyCrypto());

			MessageElement embedded = new MessageElement(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
			embedded.addChild(binaryToken);

			MessageElement wseTokenRef = new MessageElement(GenesisIIConstants.WSSE11_NS_SECURITY_QNAME);
			wseTokenRef.addChild(embedded);

			return wseTokenRef;

		} catch (GenesisIISecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (WSSecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (SOAPException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (IOException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (CredentialException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public X509Certificate getX509v3FromSecTokenRef(MessageElement wseTokenRef) throws GeneralSecurityException
	{
		MessageElement element = wseTokenRef
			.getChildElement(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
		if (element != null) {
			element = element.getChildElement(BinarySecurity.TOKEN_BST);
			if (element != null) {
				try {
					X509Security bstToken = new X509Security(element);
					return bstToken.getX509Certificate(new GIIBouncyCrypto());
				} catch (GenesisIISecurityException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (WSSecurityException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (IOException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				} catch (CredentialException e) {
					throw new GeneralSecurityException(e.getMessage(), e);
				}
			}
		}
		throw new GeneralSecurityException("Message element does not contain a certificate");
	}

	static public MessageElement makeX509v3TokenRef(X509Certificate cert) throws GeneralSecurityException
	{
		try {
			MessageElement binaryToken = new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", edu.virginia.vcgr.genii.client.comm.CommConstants.X509_SECURITY_TYPE);
			binaryToken.addTextNode("");
			X509Security bstToken = new X509Security(binaryToken);
			bstToken.setX509Certificate(cert);

			MessageElement embedded = new MessageElement(new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
			embedded.addChild(binaryToken);

			MessageElement wseTokenRef = new MessageElement(GenesisIIConstants.WSSE11_NS_SECURITY_QNAME);
			wseTokenRef.addChild(embedded);

			return wseTokenRef;

		} catch (WSSecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (SOAPException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}
}
