package edu.virginia.vcgr.genii.client.comm.jetty;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.security.Password;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.resource.Resource;

import edu.virginia.vcgr.genii.security.x509.TrustAllX509TrustManager;

/**
 * A wrapper of the generic Jetty SslSocketConnector connector in which all incoming connections are
 * initially trusted.
 * 
 * (Authorization and trust-verification occur later during message-level processing)
 * 
 * @author dgm4d
 * 
 */
public class TrustAllSslSocketConnector extends SslSocketConnector
{

	static private Log _logger = LogFactory.getLog(TrustAllSslSocketConnector.class);

	private Password _password;
	private Password _keyPassword;

	/* ------------------------------------------------------------ */
	/**
	 * Constructor.
	 */
	public TrustAllSslSocketConnector()
	{
		super();
	}

	/* ------------------------------------------------------------ */
	public void setPassword(String password)
	{
		_password = Password.getPassword(PASSWORD_PROPERTY, password, null);
		super.setPassword(password);
	}

	/* ------------------------------------------------------------ */
	public void setKeyPassword(String password)
	{
		_keyPassword = Password.getPassword(KEYPASSWORD_PROPERTY, password, null);
	}

	/**
	 * ------------------------------------------------------------ Overridden to insert a trust
	 * store that allows everyone access during SSL handshake
	 */
	protected SSLServerSocketFactory createFactory() throws Exception
	{

		KeyManager[] keyManagers = null;
		InputStream keystoreInputStream = null;
		if (getKeystore() != null)
			keystoreInputStream = Resource.newResource(getKeystore()).getInputStream();
		KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
		keyStore.load(keystoreInputStream, _password == null ? null : _password.toString().toCharArray());

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getSslKeyManagerFactoryAlgorithm());
		keyManagerFactory.init(keyStore, _keyPassword == null ? null : _keyPassword.toString().toCharArray());
		keyManagers = keyManagerFactory.getKeyManagers();

		// use our trust-all trust manager
		TrustManager trustAll = new TrustAllX509TrustManager();
		TrustManager[] trustManagers = new TrustManager[1];
		trustManagers[0] = trustAll;

		SecureRandom secureRandom = getSecureRandomAlgorithm() == null ? null : SecureRandom
			.getInstance(getSecureRandomAlgorithm());

		SSLContext context = getProvider() == null ? SSLContext.getInstance(getProtocol()) : SSLContext.getInstance(
			getProtocol(), getProvider());
		SSLContext sslcontext = context;

		SSLSessionContext sessionContext = sslcontext.getServerSessionContext();
		if (sessionContext == null) {
			if (_logger.isDebugEnabled())
				_logger.debug("Couldn't get a session context on which to set the cache size.");
		} else {
			if (sessionContext.getSessionCacheSize() > 1000) {
				if (_logger.isDebugEnabled())
					_logger.debug("Setting server ssl session context cache size to 1000.");
				sessionContext.setSessionCacheSize(1000);
			}
		}

		context.init(keyManagers, trustManagers, secureRandom);

		sessionContext = sslcontext.getServerSessionContext();
		if (sessionContext == null) {
			if (_logger.isDebugEnabled())
				_logger.debug("Couldn't get a session context on which to set the cache size.");
		} else {
			if (sessionContext.getSessionCacheSize() > 1000) {
				if (_logger.isDebugEnabled())
					_logger.debug("Setting server ssl session context cache size to 1000.");
				sessionContext.setSessionCacheSize(1000);
			}
		}

		return context.getServerSocketFactory();
	}

}
