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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.virginia.vcgr.genii.common.security.CertificateChainType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.axis.AxisClientInvocationHandler;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.IdentityAttribute;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

public class SecurityUtils
{
	static
	{
		CertTool.loadBCProvider();
	}

	static private KeyStore __trustStore = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads.
	 */
	static
	{
		ConfigurationManager
				.addConfigurationUnloadListener(new ConfigUnloadListener());
	}

	public static class ConfigUnloadListener implements
			ConfigurationUnloadedListener
	{
		public void notifyUnloaded()
		{
			synchronized (AxisClientInvocationHandler.class)
			{
				__trustStore = null;
			}
		}
	}

	/**
	 * Establishes the trust manager for use in verifying resource identities
	 */
	static public synchronized KeyStore getTrustStore()
			throws GeneralSecurityException
	{

		if (__trustStore != null)
		{
			return __trustStore;
		}

		try
		{
			Security security = Installation.getDeployment(
				new DeploymentName()).security();
			String trustStoreLoc = security.getProperty(
				SecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_LOCATION_PROP);
			String trustStoreType = security.getProperty(
				SecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_TYPE_PROP,
				SecurityConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = security.getProperty(
				SecurityConstants.Client.RESOURCE_IDENTITY_TRUST_STORE_PASSWORD_PROP);

			// open the trust store
			if (trustStoreLoc == null)
			{
				throw new GenesisIISecurityException(
						"Could not load TrustManager: no identity trust store location specified");
			}
			char[] trustStorePassChars = null;
			if (trustStorePass != null)
			{
				trustStorePassChars = trustStorePass.toCharArray();
			}
			__trustStore =CertTool.openStoreDirectPath(
				Installation.getDeployment(
					new DeploymentName()).security().getSecurityFile(trustStoreLoc),
				trustStoreType, trustStorePassChars);
			return __trustStore;

		}
		catch (ConfigurationException e)
		{
			throw new GeneralSecurityException("Could not load TrustManager: "
					+ e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new GeneralSecurityException("Could not load TrustManager: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Verify the certificate path as correctly chaining to a trusted root
	 */
	static public void validateCertPath(X509Certificate[] certChain)
			throws GeneralSecurityException
	{
		List<X509Certificate> certchain = new ArrayList<X509Certificate>();
		for (int i = 0; i < certChain.length - 1; i++)
		{
			certchain.add(certChain[i]);
		}
		CertPath cp =
				CertificateFactory.getInstance("X.509", "BC").generateCertPath(
						certchain);

		CertPathValidator cpv = CertPathValidator.getInstance("PKIX", "BC");
		PKIXParameters param =
				new PKIXParameters(SecurityUtils.getTrustStore());
		param.setRevocationEnabled(false);
		cpv.validate(cp, param);
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

	static public final PublicKey deserializePublicKey(byte[] data)
			throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (PublicKey) ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}

	/**
	 * 
	 * Note: Use the WSSecurityUtils for serializing to WS-Security XML
	 * 
	 * @param cert
	 * @return
	 * @throws IOException
	 */
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

	static public final X509Certificate deserializeX509Certificate(byte[] data)
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

	/**
	 * 
	 * Note: Use the WSSecurityUtils for serializing to WS-Security XML
	 * 
	 * @param certs
	 * @return
	 * @throws IOException
	 */
	static public final byte[][] serializeX509CertificateChain(
			X509Certificate[] certs) throws IOException
	{
		byte[][] ret = new byte[certs.length][];
		int lcv = 0;
		for (X509Certificate cert : certs)
		{
			ret[lcv++] = serializeX509Certificate(cert);
		}

		return ret;
	}

	static public final X509Certificate[] deserializeX509CertificateChain(
			byte[][] data) throws IOException, ClassNotFoundException
	{
		X509Certificate[] ret = new X509Certificate[data.length];

		for (int i = 0; i < data.length; i++)
		{
			ret[i] = deserializeX509Certificate(data[i]);
		}

		return ret;
	}

	static public final X509Certificate[] decodeCertificateChain(
			CertificateChainType certChain) throws GeneralSecurityException
	{
		int numCerts = certChain.getCount();
		X509Certificate[] certs = new X509Certificate[numCerts];

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for (int i = 0; i < numCerts; i++)
		{
			byte[] encoded = certChain.getCertificate(i);
			certs[i] =
					(X509Certificate) cf
							.generateCertificate(new ByteArrayInputStream(
									encoded));
		}
		return certs;
	}
	
	static public Collection<Identity> getCallerIdentities(
		ICallingContext callingContext) 
			throws AuthZSecurityException, GeneralSecurityException
	{
		try
		{
			
			Collection<Identity> ret = new ArrayList<Identity>();
			
			if (callingContext == null)
				throw new AuthZSecurityException(
					"Error processing GAML credential: No calling context");
			
			// remove/renew stale creds/attributes
			ClientUtils.checkAndRenewCredentials(callingContext, new Date());

			TransientCredentials transientCredentials = 
				TransientCredentials.getTransientCredentials(callingContext);
			
			for (GamlCredential cred : transientCredentials._credentials) 
			{
				/* If the cred is an Identity, then we simply add that idendity
				 * to our identity list.
				 */
				if (cred instanceof Identity) 
				{
					ret.add((Identity)cred);
				} else if (cred instanceof SignedAssertion) 
				{
					/* If the cred is a signed assertion, then we have to
					 * get the identity out of the assertion.
					 */
					SignedAssertion signedAssertion = (SignedAssertion)cred;
					
					// if its an identity assertion, check it against our ACLs
					if (signedAssertion.getAttribute() 
						instanceof IdentityAttribute) 
					{
						IdentityAttribute identityAttr = 
							(IdentityAttribute) signedAssertion.getAttribute();
	
						ret.add(identityAttr.getIdentity());
					}
				}
			}
			
			return ret;
		}
		catch (IOException ioe)
		{
			throw new AuthZSecurityException("Unable to load current context.", 
				ioe);
		}
	}
	
	static public Collection<Identity> getCallerIdentities()
		throws AuthZSecurityException, IOException, GeneralSecurityException
	{
		return getCallerIdentities(ContextManager.getCurrentContext());
	}
}