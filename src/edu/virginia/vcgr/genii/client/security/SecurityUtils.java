package edu.virginia.vcgr.genii.client.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.soap.SOAPException;
import javax.xml.namespace.QName;


import edu.virginia.vcgr.genii.common.security.CertificateChainType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.axis.AxisClientInvocationHandler;
import edu.virginia.vcgr.genii.client.comm.axis.security.FlexibleBouncyCrypto;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.utils.deployment.DeploymentRelativeFile;

import org.apache.axis.message.MessageElement;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.PKIPathSecurity;
import org.apache.ws.security.message.token.X509Security;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.morgan.util.io.StreamUtils;

public class SecurityUtils
{
	private static final String TS_LOCATION = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-location";
	private static final String TS_TYPE = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-type";
	private static final String TS_PASSWORD = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-password";

	static {
		CertTool.loadBCProvider();
	}
    
    static private KeyStore __trustStore = null;
	
	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads. 
	 */
	static {
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}
	public static class ConfigUnloadListener implements ConfigurationUnloadedListener {
		public void notifyUnloaded() {
			synchronized(AxisClientInvocationHandler.class) { 
				__trustStore = null;
			}
		}
	}	
	
	/**
	 * Establishes the trust manager for use in verifying resource identities 
	 */
	static public synchronized KeyStore getTrustStore() throws GeneralSecurityException {

		if (__trustStore != null) {
			return __trustStore;
		}
		
		try {
			XMLConfiguration conf = 
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
			Properties resourceIdSecProps = (Properties) conf.retrieveSection(
					GenesisIIConstants.RESOURCE_IDENTITY_PROPERTIES_SECTION_NAME);			

			String trustStoreLoc = resourceIdSecProps.getProperty(
				TS_LOCATION);
			String trustStoreType = resourceIdSecProps.getProperty(
				TS_TYPE, GenesisIIConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = resourceIdSecProps.getProperty(
				TS_PASSWORD);
			
			// open the trust store
			if (trustStoreLoc == null) {
				throw new GenesisIISecurityException("Could not load TrustManager: no identity trust store location specified");
			}
			char[] trustStorePassChars = null;
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}
			__trustStore = CertTool.openStoreDirectPath(
				new DeploymentRelativeFile(trustStoreLoc), trustStoreType, trustStorePassChars);
			return __trustStore;

		} catch (ConfigurationException e) { 
			throw new GeneralSecurityException("Could not load TrustManager: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new GeneralSecurityException("Could not load TrustManager: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Verify the certificate path as correctly chaining to a trusted root
	 */	
	static public void validateCertPath(X509Certificate[] certChain) throws GeneralSecurityException {
		List<X509Certificate> certchain = new ArrayList<X509Certificate>();
        for (int i = 0; i < certChain.length - 1; i++) {
        	certchain.add(certChain[i]);
        }
        CertPath cp = CertificateFactory.getInstance("X.509", "BC").
        	generateCertPath(certchain);

        CertPathValidator cpv = CertPathValidator.getInstance("PKIX", "BC");
        PKIXParameters param = new PKIXParameters(SecurityUtils.getTrustStore());
        param.setRevocationEnabled(false);
        cpv.validate(cp, param);  
	}	
	
	static public X509Certificate[] getChainFromPkiPathSecTokenRef(MessageElement wseTokenRef) 
			throws GeneralSecurityException { 
		
		MessageElement element = wseTokenRef.getChildElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
		if (element != null) {
			element = element.getChildElement(
					BinarySecurity.TOKEN_BST);
			if (element != null) {
				try {
					PKIPathSecurity bstToken = new PKIPathSecurity(element);
					return bstToken.getX509Certificates(false, 
						new FlexibleBouncyCrypto());
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
	
	
	static public MessageElement makePkiPathSecTokenRef(X509Certificate[] certChain) 
			throws GeneralSecurityException {
		
		try {
		
			MessageElement binaryToken = new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", PKIPathSecurity.getType());
			binaryToken.addTextNode("");
			BinarySecurity bstToken = new PKIPathSecurity(binaryToken);
			((PKIPathSecurity) bstToken).setX509Certificates(
					certChain, 
					false,
					new FlexibleBouncyCrypto());
		
			MessageElement embedded = new MessageElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
			embedded.addChild(binaryToken);
		
			MessageElement wseTokenRef = new MessageElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"));
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
	
	static public X509Certificate getX509v3FromSecTokenRef(MessageElement wseTokenRef) 
			throws GeneralSecurityException { 
		
		MessageElement element = wseTokenRef.getChildElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
		if (element != null) {
			element = element.getChildElement(
					BinarySecurity.TOKEN_BST);
			if (element != null) {
				try {
					X509Security bstToken = new X509Security(element);
					return bstToken.getX509Certificate(new FlexibleBouncyCrypto());
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


	static public MessageElement makeX509v3TokenRef(X509Certificate cert) 
			throws GeneralSecurityException {

		try {
		
			MessageElement binaryToken = new MessageElement(BinarySecurity.TOKEN_BST);
			binaryToken.setAttributeNS(null, "ValueType", X509Security.getType());
			binaryToken.addTextNode("");
			X509Security bstToken = new X509Security(binaryToken);
			bstToken.setX509Certificate(cert);
		
			MessageElement embedded = new MessageElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "Embedded"));
			embedded.addChild(binaryToken);
		
			MessageElement wseTokenRef = new MessageElement(
				new QName(org.apache.ws.security.WSConstants.WSSE11_NS, "SecurityTokenReference"));
			wseTokenRef.addChild(embedded);
			
			return wseTokenRef;
		
		} catch (WSSecurityException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		} catch (SOAPException e) {
			throw new GeneralSecurityException(e.getMessage(), e);
		}			
	}
	
	
	static public final byte[] serializePublicKey(PublicKey pk)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(pk);
			oos.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(oos);
		}
	}
	
	static public final PublicKey deserializePublicKey(byte []data)
		throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (PublicKey)ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}
	
	static public final byte[] serializeX509Certificate(X509Certificate cert)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(cert);
			oos.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(oos);
		}
	}

	static public final X509Certificate deserializeX509Certificate(byte []data)
		throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
	
		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (X509Certificate) ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}	
	}
	
	static public final byte[][] serializeX509CertificateChain(X509Certificate [] certs)
		throws IOException
	{
		byte [][]ret = new byte[certs.length][];
		int lcv = 0;
		for (X509Certificate cert : certs)
		{
			ret[lcv++] = serializeX509Certificate(cert);
		}
	
		return ret;
	}

	static public final X509Certificate [] deserializeX509CertificateChain(byte [][] data)
		throws IOException, ClassNotFoundException
	{
		X509Certificate [] ret = new X509Certificate[data.length];

		for (int i = 0; i < data.length; i++)
		{
			ret[i] = deserializeX509Certificate(data[i]);
		}

		return ret;
	}
	
	static public final X509Certificate [] decodeCertificateChain(CertificateChainType certChain)
		throws GeneralSecurityException
	{
		int numCerts = certChain.getCount();
		X509Certificate [] certs = new X509Certificate[numCerts];

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for (int i = 0; i < numCerts; i++) {
			byte [] encoded = certChain.getCertificate(i);
			certs[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encoded));
		}
		return certs;
	}
}