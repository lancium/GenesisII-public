package edu.virginia.vcgr.genii.security.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.morgan.util.io.StreamUtils;

import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import edu.virginia.vcgr.genii.security.CertificateValidator;
import edu.virginia.vcgr.genii.security.identity.Identity;
import eu.emi.security.authn.x509.CommonX509TrustManager;
import eu.emi.security.authn.x509.impl.InMemoryKeystoreCertChainValidator;

public class SecurityUtilities implements CertificateValidator
{
	static private Log _logger = LogFactory.getLog(SecurityUtilities.class);

	private KeyStore _localResourceTrustStore = null;
	private static Boolean loadedSecurity = false;

	public SecurityUtilities(KeyStore localResourceTrustStore)
	{
		_localResourceTrustStore = localResourceTrustStore;
	}

	@Override
	public KeyStore getResourceTrustStore()
	{
		return _localResourceTrustStore;
	}

	/**
	 * Set up our cryptography provider.
	 */
	public static void initializeSecurity()
	{
		synchronized (loadedSecurity) {
			if (!loadedSecurity) {
				Security.addProvider(new BouncyCastleProvider());
				loadedSecurity = true;
			}
		}
	}

	/**
	 * Verifies that the certificate chain is internally consistent.
	 */
	@Override
	public boolean validateCertificateConsistency(X509Certificate[] certChain)
	{
		return simpleCertificateValidation(certChain);
	}

	/**
	 * Verifies that the certificate is found rooted in our local resource trust store.
	 */
	@Override
	public boolean validateIsTrustedResource(X509Certificate[] certChain)
	{
		return validateTrustedByKeystore(certChain, _localResourceTrustStore);
	}

	/**
	 * Verifies that the certificate is found rooted in the specified trust store.
	 */
	@Override
	public boolean validateTrustedByKeystore(X509Certificate[] certChain, KeyStore store)
	{
		return simpleCertificateValidation(certChain) && creatorInStore(certChain, store)
			&& isAnchoredInKeystore(certChain, store);
	}

	/**
	 * utility method that prints out all of the aliases and DNs in the key store.
	 */
	static public String showTrustStore(KeyStore toShow)
	{
		StringBuilder toReturn = new StringBuilder();
		try {
			Enumeration<String> aliases = toShow.aliases();
			if (aliases != null) {
				while (aliases.hasMoreElements()) {
					String alias = aliases.nextElement();
					toReturn.append("trusted alias: ");
					toReturn.append(alias);
					toReturn.append(" for DN: ");
					Certificate c = toShow.getCertificate(alias);
					if (c instanceof X509Certificate)
						toReturn.append(((X509Certificate) c).getSubjectDN());
					else
						toReturn.append("UNKNOWN");
					toReturn.append("\n");
				}
			}
		} catch (KeyStoreException e) {
			_logger.error("failure to show trust store.", e);
		}
		return toReturn.toString();
	}

	/**
	 * Simply verify each certificate with its predecessor.
	 */
	public static boolean simpleCertificateValidation(X509Certificate[] certChain)
	{
		for (int i = 0; i < certChain.length - 2; i++) {
			try {
				certChain[i].verify(certChain[i + 1].getPublicKey());
			} catch (Throwable e) {
				_logger.error("failure to validate internal consistency of this cert " + certChain[i].getSubjectDN()
					+ " error is: " + e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	/*
	 * Returns true if the creator of a certChain is located in the trust store.
	 */
	static public boolean creatorInStore(X509Certificate[] certChain, KeyStore store)
	{
		try {
			Enumeration<String> aliases = store.aliases();
			if (aliases != null) {
				while (aliases.hasMoreElements()) {
					String alias = aliases.nextElement();
					Certificate c = store.getCertificate(alias);
					if (c instanceof X509Certificate) {
						if (c.equals(certChain[0])) {
							if (_logger.isDebugEnabled())
								_logger.debug("found identical certificate in trust store under alias '" + alias + "'");
							return true;
						}
						try {
							certChain[0].verify(c.getPublicKey());
							if (_logger.isDebugEnabled())
								_logger.debug("found creator for " + certChain[0].getSubjectDN() + " in store under alias '"
									+ alias + "'");
							return true;
						} catch (Throwable e) {
							// stomp the exception since that wasn't the creator.
						}
					}
				}
			}
		} catch (KeyStoreException e) {
			_logger.error("failure to enumerate aliases in trust store.", e);
		}
		return false;
	}

	public static boolean isAnchoredInKeystore(X509Certificate[] certChain, KeyStore store)
	{
		KeyStore ks = store;
		boolean trustOkay = false;
		/*
		 * hmmm: may want to cache both types of trust managers in different caches, indexed by the
		 * keystore.
		 */
		try {
			// create a trust manager from the trust store by trying jdk ssl support first.
			PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(ks, new X509CertSelector());
			pkixParams.setRevocationEnabled(false);
			ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters(pkixParams);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(trustParams);
			X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
			trustManager.checkClientTrusted(certChain, certChain[0].getPublicKey().getAlgorithm());
			if (_logger.isDebugEnabled())
				_logger.debug("validated cert with jdk ssl: " + certChain[0].getSubjectDN());
			trustOkay = true;
		} catch (Throwable e) {
			if (_logger.isDebugEnabled())
				_logger.debug("could not validate this cert with jdk ssl against trust store: " + certChain[0].getSubjectDN()
					+ e.getMessage());
		}
		try {
			if (!trustOkay) {
				InMemoryKeystoreCertChainValidator validater = new InMemoryKeystoreCertChainValidator(ks);
				CommonX509TrustManager trustManager = new CommonX509TrustManager(validater);
				trustManager.checkClientTrusted(certChain, certChain[0].getPublicKey().getAlgorithm());
				if (_logger.isDebugEnabled())
					_logger.debug("validated cert: " + certChain[0].getSubjectDN());
				trustOkay = true;
			}
		} catch (Throwable e) {
			if (_logger.isDebugEnabled())
				_logger.debug("could not validate this cert with CANL against trust store: " + certChain[0].getSubjectDN(), e);
		}
		return trustOkay;
	}

	static public final byte[] serializePublicKey(PublicKey pk) throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(pk);
			oos.flush();
			return baos.toByteArray();
		} finally {
			StreamUtils.close(oos);
		}
	}

	static public final PublicKey deserializePublicKey(byte[] data) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try {
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (PublicKey) ois.readObject();
		} finally {
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
	static public final byte[] serializeX509Certificate(X509Certificate cert) throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(cert);
			oos.flush();
			return baos.toByteArray();
		} finally {
			StreamUtils.close(oos);
		}
	}

	static public final X509Certificate deserializeX509Certificate(byte[] data) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try {
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (X509Certificate) ois.readObject();
		} finally {
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
	static public final byte[][] serializeX509CertificateChain(X509Certificate[] certs) throws IOException
	{
		byte[][] ret = new byte[certs.length][];
		int lcv = 0;
		for (X509Certificate cert : certs) {
			ret[lcv++] = serializeX509Certificate(cert);
		}

		return ret;
	}

	static public final X509Certificate[] deserializeX509CertificateChain(byte[][] data) throws IOException,
		ClassNotFoundException
	{
		X509Certificate[] ret = new X509Certificate[data.length];

		for (int i = 0; i < data.length; i++) {
			ret[i] = deserializeX509Certificate(data[i]);
		}

		return ret;
	}

	static final public Pattern GROUP_TOKEN_PATTERN = Pattern
		.compile("^.*(?<![a-z])cn=[^,]*group.*$", Pattern.CASE_INSENSITIVE);
	static final public Pattern CLIENT_IDENTITY_PATTERN = Pattern.compile("^.*(?<![a-z])cn=[^,]*Client.*$",
		Pattern.CASE_INSENSITIVE);

	static private boolean matches(Identity identity, Pattern[] patterns)
	{
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(identity.toString());
			if (matcher.matches())
				return true;
		}

		return false;
	}

	static public Collection<Identity> filterCredentials(Collection<Identity> in, Pattern... patterns)
	{
		Collection<Identity> ret = new ArrayList<Identity>(in.size());

		for (Identity test : in) {
			if (!matches(test, patterns))
				ret.add(test);
		}

		return ret;
	}

	static public List<Certificate> loadCertificatesFromDirectory(File directory)
	{
		if (directory == null || !directory.isDirectory())
			return Collections.emptyList();

		List<Certificate> certificateList = new ArrayList<Certificate>();
		File[] certificateFiles = directory.listFiles();

		for (File certificateFile : certificateFiles) {
			try {
				// skip hidden files, i.e. those that start with a dot.
				char testHidden = certificateFile.getName().charAt(0);
				if (testHidden == '.')
					continue;
				FileInputStream fis = new FileInputStream(certificateFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				while (bis.available() > 0) {
					Certificate certificate = cf.generateCertificate(bis);
					certificateList.add(certificate);
				}
				fis.close();
				if (_logger.isDebugEnabled())
					_logger.debug("Loaded trusted certificate(s) from file: " + certificateFile.getName());
			} catch (Exception ex) {
				_logger.warn("Failed to load certificates from file: " + certificateFile.getName(), ex);
			}
		}
		if (_logger.isDebugEnabled())
			_logger.debug("Loaded " + certificateFiles.length + " certificates from trusted directory.");

		return certificateList;
	}

	static public KeyStore createTrustStoreFromCertificates(String proposedTrustStoreType, String password,
		List<Certificate> certificateList)
	{

		KeyStore trustStore = null;
		char[] trustStorePassword = (password == null) ? "genesisII".toCharArray() : password.toCharArray();

		Set<String> trustStoreTypes = new HashSet<String>();
		trustStoreTypes.add(proposedTrustStoreType);
		trustStoreTypes.add("JKS");
		trustStoreTypes.add(KeyStore.getDefaultType());

		for (String type : trustStoreTypes) {
			try {
				trustStore = KeyStore.getInstance(type);
				trustStore.load(null, trustStorePassword);
				int certificateIndex = 0;
				for (Certificate certificate : certificateList) {
					final String alias = "trusted_certificate_" + certificateIndex;
					trustStore.setCertificateEntry(alias, certificate);
					certificateIndex++;
				}
				// Successfully loaded all the certificates. Don't have to try the other options.
				break;
			} catch (Exception ex) {
			}
		}
		return trustStore;
	}

	/**
	 * returns a standard PEM representation for the certificate.
	 */
	public static String convertX509ToPem(X509Certificate toConvert)
	{
		BASE64Encoder encoder = new BASE64Encoder();
		StringBuilder s = new StringBuilder();
		s.append(X509Factory.BEGIN_CERT + "\n");
		try {
			s.append(encoder.encodeBuffer(toConvert.getEncoded()));
		} catch (CertificateEncodingException e) {
			s.append("Failed to encode certificate as bytes");
		}
		s.append(X509Factory.END_CERT + "\n");
		return s.toString();
	}

	/**
	 * returns a checksum value across the encoded certificate.
	 */
	public static long getChecksum(X509Certificate cert)
	{
		try {
			byte[] encCert = cert.getEncoded();
			CRC32 c = new CRC32();
			c.update(encCert, 0, encCert.length);
			return c.getValue();
		} catch (Exception e) {
			_logger.error("problem encoding checksum.");
			return -1;
		}
	}

	/**
	 * returns a checksum value across the encoded public key.
	 */
	public static long getChecksum(PublicKey pk)
	{
		try {
			byte[] encCert = pk.getEncoded();
			CRC32 c = new CRC32();
			c.update(encCert, 0, encCert.length);
			return c.getValue();
		} catch (Exception e) {
			_logger.error("problem encoding checksum.");
			return -1;
		}
	}
}
