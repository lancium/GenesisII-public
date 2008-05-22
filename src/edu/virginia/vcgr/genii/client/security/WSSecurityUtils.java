package edu.virginia.vcgr.genii.client.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.*;

import javax.xml.soap.SOAPException;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;

import org.apache.axis.message.MessageElement;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.message.token.X509Security;

/**
 * Operations for converting popular token types to/from MessageElements
 * 
 * @author dmerrill
 * 
 */
public class WSSecurityUtils
{

	public static final String GAML_ATTR_TOKEN_TYPE =
			"http://vcgr.cs.virginia.edu/security/2007/11/attr-saml";
	public static final String DELEGATED_GAML_TOKEN_TYPE =
			"http://vcgr.cs.virginia.edu/security/2007/11/delegated-saml";

	public static final String PKCS7_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#PKCS7";
	public static final String X509v3_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";
	public static final String X509PKIPathv1_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

	public static final String PASSWORD_DIGEST_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest";
	public static final String PASSWORD_TEXT_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
	public static final String USERNAME_TOKEN_URI =
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#UsernameToken";

	static public String getNameTokenFromUTSecTokenRef(
			MessageElement wseTokenRef) throws GeneralSecurityException
	{

		try
		{
			UsernameToken bstToken = new UsernameToken(wseTokenRef);
			return bstToken.getName();
		}
		catch (WSSecurityException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public String getPasswordTokenFromUTSecTokenRef(
			MessageElement wseTokenRef) throws GeneralSecurityException
	{

		try
		{
			UsernameToken bstToken = new UsernameToken(wseTokenRef);
			return bstToken.getPassword();
		}
		catch (WSSecurityException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public MessageElement makeUTSecTokenRef(String username,
			String password) throws GeneralSecurityException
	{

		try
		{
			MessageElement userElem =
					new MessageElement(WSConstants.WSSE_NS, "Username");
			userElem.addTextNode(username);
			MessageElement passElem =
					new MessageElement(WSConstants.WSSE_NS, "Password");
			passElem.addTextNode(password);
			MessageElement token = new MessageElement(UsernameToken.TOKEN);
			token.addChild(userElem);
			token.addChild(passElem);

			return token;

		}
		catch (Exception e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public X509Certificate[] getChainFromPkiPathSecTokenRef(
			MessageElement wseTokenRef) throws GeneralSecurityException
	{

		MessageElement element =
				wseTokenRef.getChildElement(new QName(
						org.apache.ws.security.WSConstants.WSSE11_NS,
						"Embedded"));
		if (element != null)
		{
			element = element.getChildElement(BinarySecurity.TOKEN_BST);
			if (element != null)
			{
				try
				{
					PKIPathSecurity bstToken = new PKIPathSecurity(element);
					return bstToken.getX509Certificates(false,
							new FlexibleBouncyCrypto());
				}
				catch (GenesisIISecurityException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (WSSecurityException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (IOException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (CredentialException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
			}
		}
		throw new GeneralSecurityException(
				"Message element does not contain a PKIPath certificate chain");
	}

	static public MessageElement makePkiPathSecTokenRef(
			X509Certificate[] certChain) throws GeneralSecurityException
	{
		return makePkiPathSecTokenRef(certChain, null);
	}

	static public MessageElement makePkiPathSecTokenRef(
			X509Certificate[] certChain, String wsuId)
			throws GeneralSecurityException
	{

		try
		{

			MessageElement binaryToken =
					new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", PKIPathSecurity
					.getType());
			if (wsuId != null)
			{
				binaryToken.setAttribute(WSConstants.WSU_NS, "Id", wsuId);
			}
			binaryToken.addTextNode("");
			BinarySecurity bstToken = new PKIPathSecurity(binaryToken);
			((PKIPathSecurity) bstToken).setX509Certificates(certChain, false,
					new FlexibleBouncyCrypto());

			MessageElement embedded =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"Embedded"));
			embedded.addChild(binaryToken);

			MessageElement wseTokenRef =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference"));
			wseTokenRef.addChild(embedded);

			return wseTokenRef;

		}
		catch (GenesisIISecurityException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (WSSecurityException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (SOAPException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (CredentialException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	static public X509Certificate getX509v3FromSecTokenRef(
			MessageElement wseTokenRef) throws GeneralSecurityException
	{

		MessageElement element =
				wseTokenRef.getChildElement(new QName(
						org.apache.ws.security.WSConstants.WSSE11_NS,
						"Embedded"));
		if (element != null)
		{
			element = element.getChildElement(BinarySecurity.TOKEN_BST);
			if (element != null)
			{
				try
				{
					X509Security bstToken = new X509Security(element);
					return bstToken
							.getX509Certificate(new FlexibleBouncyCrypto());
				}
				catch (GenesisIISecurityException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (WSSecurityException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (IOException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
				catch (CredentialException e)
				{
					throw new GeneralSecurityException(e.getMessage(), e);
				}
			}
		}
		throw new GeneralSecurityException(
				"Message element does not contain a certificate");
	}

	static public MessageElement makeX509v3TokenRef(X509Certificate cert)
			throws GeneralSecurityException
	{

		try
		{

			MessageElement binaryToken =
					new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", X509Security
					.getType());
			binaryToken.addTextNode("");
			X509Security bstToken = new X509Security(binaryToken);
			bstToken.setX509Certificate(cert);

			MessageElement embedded =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"Embedded"));
			embedded.addChild(binaryToken);

			MessageElement wseTokenRef =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference"));
			wseTokenRef.addChild(embedded);

			return wseTokenRef;

		}
		catch (WSSecurityException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
		catch (SOAPException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	public static GamlCredential decodeTokenElement(MessageElement tokenElement)
			throws GeneralSecurityException
	{

		// try delegated/signed assertion
		try
		{
			MessageElement element = tokenElement;
			if (element.getQName().equals(
					new QName(org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference")))
			{
				element =
						element.getChildElement(new QName(
								org.apache.ws.security.WSConstants.WSSE11_NS,
								"Embedded"));
				if (element != null)
				{
					element = element.getChildElement(BinarySecurity.TOKEN_BST);
					if (element != null)
					{
						String valueAttr =
								element.getAttributeValue("ValueType");
						if ((valueAttr != null)
								&& (valueAttr
										.equals(WSSecurityUtils.DELEGATED_GAML_TOKEN_TYPE)))
						{
							String encodedValue = element.getValue();
							return SignedAssertionBaseImpl
									.base64decodeAssertion(encodedValue);
						}
					}
				}
			}
			// try X509 identity
			return new X509Identity(tokenElement);
		}
		catch (Exception e)
		{
			try
			{
				return new UsernamePasswordIdentity(tokenElement);
			}
			catch (Exception e2)
			{
			}
		}

		return null;
	}

}