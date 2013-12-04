package edu.virginia.vcgr.genii.client.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.mortbay.jetty.security.SslSocketConnector;

public class SslInformation
{
	static private Log _logger = LogFactory.getLog(SslInformation.class);

	private String _keystoreFilename;
	private String _keystoreType;
	private String _keystorePassword;
	private String _keyPassword;

	private Security _properties;

	public SslInformation(Security properties)
	{
		_properties = properties; // save the properties.
		// load our tls keystore filename and its properties.
		_keystoreFilename = properties.getProperty(KeystoreSecurityConstants.Container.SSL_KEY_STORE_PROP);
		if (_keystoreFilename == null) {
			String msg = "Required ssl property \"" + KeystoreSecurityConstants.Container.SSL_KEY_STORE_PROP + "\" not found.";
			_logger.error(msg);
			throw new ConfigurationException(msg);
		}
		// make the tls keystore path absolute.
		_keystoreFilename =
			(Installation.getDeployment(new DeploymentName()).security().getSecurityFile(_keystoreFilename)).getAbsolutePath();
		_keystoreType = properties.getProperty(KeystoreSecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP);
		if (_keystoreType == null) {
			String msg =
				"Required ssl property \"" + KeystoreSecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP + "\" not found.";
			_logger.error(msg);
			throw new ConfigurationException(msg);
		}
		// get the password on the file and the password on the key, if needed.
		_keystorePassword = properties.getProperty(KeystoreSecurityConstants.Container.SSL_KEY_STORE_PASSWORD_PROP);
		_keyPassword = properties.getProperty(KeystoreSecurityConstants.Container.SSL_KEY_PASSWORD_PROP);
	}

	public void configure(ConfigurationManager manager, SslSocketConnector connector)
	{
		connector.setKeystore(_keystoreFilename);
		connector.setKeystoreType(_keystoreType);
		connector.setPassword(_keystorePassword);
		connector.setKeyPassword(_keyPassword);

		// request clients to authn with a cert
		connector.setWantClientAuth(true);
	}

	// simple wrapper for the two strings needed to authorize against kerberos realm.
	public class KerberosKeytabAndPrincipal
	{
		public String _keytab;
		public String _principal;

		public KerberosKeytabAndPrincipal(String keytab, String principal)
		{
			_keytab = keytab;
			_principal = principal;
		}
	}

	public KerberosKeytabAndPrincipal loadKerberosKeytable(String realm)
	{
		// load the keytab to authorize the service principal for this realm and our host.
		String keytabPropertyName = KeystoreSecurityConstants.Kerberos.keytabPropertyForRealm(realm);
		String principalPropertyName = KeystoreSecurityConstants.Kerberos.principalPropertyForRealm(realm);

		String keytabFile = (String) _properties.getProperty(keytabPropertyName);
		String principal = (String) _properties.getProperty(principalPropertyName);

		if ((keytabFile == null) || (principal == null)) {
			_logger.warn("Could not find the keytab or principal properties for realm " + realm + " on this host.");
			return null;
		}

		return new KerberosKeytabAndPrincipal(keytabFile, principal);
	}

	public String getKeystoreFilename()
	{
		return _keystoreFilename;
	}

	public String getKeystoreType()
	{
		return _keystoreType;
	}

	public String getKeystorePassword()
	{
		return _keystorePassword;
	}

	public String getKeyPassword()
	{
		return _keyPassword;
	}
}
