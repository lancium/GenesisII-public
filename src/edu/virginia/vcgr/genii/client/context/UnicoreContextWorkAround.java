package edu.virginia.vcgr.genii.client.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Properties;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

final public class UnicoreContextWorkAround
{
	static final public String DELEGATEE_KEY_AND_CERT_DIR_ENVVAR =
		"GENII_UNICORE_DELEGATEE_DIR";
	
	static final public String DELEGATEE_KEYSTORE_FILENAME =
		"delegatee.pfx";
	static final public String DELEGATEE_PROPERTIES_FILENAME =
		"delegatee.properties";
	
	static final public String DELEGATEE_KEYSTORE_PASSWORD_PROPERTY =
		"delegatee.keystore.password";
	static final public String DELEGATEE_KEY_PASSWORD_PROPERTY =
		"delegatee.key.password";
	static final public String DELEGATEE_CERTIFICATE_ALIAS_PROPERTY =
		"delegatee.certificate.alias";
	static final public String DELEGATEE_KEY_ALIAS_PROPERTY =
		"delegatee.key.alias";
	
	static private Properties readProperties(File propertiesFile)
		throws FileNotFoundException, IOException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(propertiesFile);
			Properties ret = new Properties();
			ret.load(in);
			return ret;
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private String readRequiredProperty(
		Properties properties, String propertyName) throws IOException
	{
		String value = properties.getProperty(propertyName);
		if (value == null)
			throw new IOException(String.format(
				"Required property \"%s\" not defined!",
				propertyName));
		
		return value;
	}
	
	static public KeyAndCertMaterial loadUnicoreContextDelegateeInformation()
		throws IOException, GeneralSecurityException
	{
		String dirString = System.getenv(DELEGATEE_KEY_AND_CERT_DIR_ENVVAR);
		if (dirString == null)
			throw new IOException(String.format(
				"Environment variable %s undefined!",
				DELEGATEE_KEY_AND_CERT_DIR_ENVVAR));
		
		File dir = new File(dirString);
		File keystoreFile = new File(dir, DELEGATEE_KEYSTORE_FILENAME);
		File propertiesFile = new File(dir, DELEGATEE_PROPERTIES_FILENAME);
		
		Properties props = readProperties(propertiesFile);
		String keystorePassword = readRequiredProperty(props,
			DELEGATEE_KEYSTORE_PASSWORD_PROPERTY);
		String keyPassword = props.getProperty(
			DELEGATEE_KEY_PASSWORD_PROPERTY, keystorePassword);
		String certAlias = props.getProperty(
			DELEGATEE_CERTIFICATE_ALIAS_PROPERTY);
		
		KeyStore store = 
			CertTool.openStoreDirectPath(keystoreFile, "PKCS12", 
				keystorePassword.toCharArray());
		if (certAlias == null)
		{
			Enumeration<String> aliases = store.aliases();
			if (!aliases.hasMoreElements())
				throw new IOException(String.format(
					"Keystore %s appears empty!", keystoreFile));
			certAlias = aliases.nextElement();
			if (aliases.hasMoreElements())
				throw new IOException(String.format(
					"Keystore %s has more than one entry and no alias was given!",
					keystoreFile));
		}
		
		String keyAlias = props.getProperty(DELEGATEE_KEY_ALIAS_PROPERTY,
			certAlias);
		
		Certificate []certChain = store.getCertificateChain(certAlias);
		if (certChain == null)
			throw new IOException(String.format(
				"Keystore %s does not contain certificate alias %s!",
				keystoreFile, certAlias));
		
		Key key = store.getKey(keyAlias, keyPassword.toCharArray());
		if (key == null)
			throw new IOException(String.format(
				"Keystore %s does not contain key alias %s!",
				keyAlias));
		
		X509Certificate []xCertChain = new X509Certificate[certChain.length];
		for (int lcv = 0; lcv < certChain.length; lcv++)
		{
			if (!(certChain[lcv] instanceof X509Certificate))
				throw new IOException(String.format(
					"Certificate %s in keystore %s is not an X.509 Certificate!",
					certAlias, keystoreFile));

			xCertChain[lcv] = (X509Certificate)certChain[lcv];
		}
		
		if (!(key instanceof PrivateKey))
			throw new IOException(String.format(
				"Key %s in keystore %s is not a private key!",
				keyAlias, keystoreFile));
		
		return new KeyAndCertMaterial(xCertChain, (PrivateKey)key);
	}
}