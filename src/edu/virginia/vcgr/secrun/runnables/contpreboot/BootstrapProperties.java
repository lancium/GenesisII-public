package edu.virginia.vcgr.secrun.runnables.contpreboot;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.postlog.PostTarget;
import edu.virginia.vcgr.genii.client.postlog.empty.EmptyPostTarget;
import edu.virginia.vcgr.genii.client.postlog.http.HttpPostTarget;

public class BootstrapProperties extends Properties
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BootstrapProperties.class);
	
	static final private String BOOTSTRAP_PROPERTIES_RESOURCE =
		"META-INF/secure-runnable/runnable-description.properties";
	
	static final public String GENII_EXCEPTION_POST_LOG_URL_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.exception-log-url";
	static final public String GENII_CONNECT_URL_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.connect-url";
	
	static final public String INSTALLER_CERT_STORE_PATH_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.installer-cert-store-path";
	static final public String INSTALLER_CERT_STORE_TYPE_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.installer-cert-store-type";
	static final public String INSTALLER_CERT_PATTERN_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.installer-cert-pattern";
	static final public String INSTALLER_CERT_STORE_PASSWORD_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.installer-cert-store-password";
	
	static final public String CERT_GENERATOR_RNS_PATH_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.rns";
	static final public String CERT_GENERATOR_OUTPUT_STORE_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.output-store";
	static final public String CONTAINER_PUBLIC_CERT_FILENAME_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.public-cert.filename";
	static final public String CERT_GENERATOR_STORE_PWORD_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.store-pword";
	static final public String CERT_GENERATOR_ALIAS_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.alias";
	static final public String CERT_GENERATOR_OU_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.ou";
	static final public String CERT_GENERATOR_O_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.o";
	static final public String CERT_GENERATOR_L_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.l";
	static final public String CERT_GENERATOR_C_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.c";
	static final public String CERT_GENERATOR_ST_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.cert-generator.st";

	
	private PostTarget _exceptionLogger = null;
	
	public BootstrapProperties()
		throws IOException
	{
		InputStream in = null;
		ClassLoader loader = BootstrapProperties.class.getClassLoader();
		try
		{
			in = loader.getResourceAsStream(
				BOOTSTRAP_PROPERTIES_RESOURCE);
			if (in == null)
				throw new ConfigurationException(
					"Couldn't find bootstrap properties resource.");
			
			load(in);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	public PostTarget getExceptionLogger()
	{
		synchronized(this)
		{
			if (_exceptionLogger == null)
			{
				String url = getProperty(
					GENII_EXCEPTION_POST_LOG_URL_PROPERTY_NAME);
				if (url == null)
					_exceptionLogger = new EmptyPostTarget();
				else
				{
					try
					{
						_exceptionLogger = new HttpPostTarget(url);
					}
					catch (MalformedURLException mue)
					{
						_exceptionLogger = new EmptyPostTarget();
						_logger.error("Unable to connect logger to \""
							+ url + "\"...defaulting to empty logger.", mue);
					}
				}
			}
		}
		
		return _exceptionLogger;
	}

	public String getConnectURL()
	{
		return getProperty(GENII_CONNECT_URL_PROPERTY_NAME);
	}
	
	public String getInstallerCertStorePath()
	{
		return getProperty(INSTALLER_CERT_STORE_PATH_PROPERTY_NAME);
	}
	
	public String getInstallerCertStoreType()
	{
		return getProperty(INSTALLER_CERT_STORE_TYPE_PROPERTY_NAME);
	}
	
	public String getInstallerCertPattern()
	{
		return getProperty(INSTALLER_CERT_PATTERN_PROPERTY_NAME);
	}
	
	public String getInstallerCertStorePassword()
	{
		return getProperty(INSTALLER_CERT_STORE_PASSWORD_PROPERTY_NAME);
	}
	
	public String getCertGeneratorRNSPath()
	{
		String ret = getProperty(CERT_GENERATOR_RNS_PATH_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_RNS_PATH_PROPERTY_NAME));
		return ret;
	}
	
	public String getCertGeneratorOutputStoreName()
	{
		String ret = getProperty(CERT_GENERATOR_OUTPUT_STORE_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_OUTPUT_STORE_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorPassword()
	{
		String ret = getProperty(CERT_GENERATOR_STORE_PWORD_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_STORE_PWORD_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorAlias()
	{
		String ret = getProperty(CERT_GENERATOR_ALIAS_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_ALIAS_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorOU()
	{
		String ret = getProperty(CERT_GENERATOR_OU_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_OU_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorO()
	{
		String ret = getProperty(CERT_GENERATOR_O_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_O_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorL()
	{
		String ret = getProperty(CERT_GENERATOR_L_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_L_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorC()
	{
		String ret = getProperty(CERT_GENERATOR_C_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_C_PROPERTY_NAME));
		return ret;
	}

	public String getCertGeneratorST()
	{
		String ret = getProperty(CERT_GENERATOR_ST_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CERT_GENERATOR_ST_PROPERTY_NAME));
		return ret;
	}

	public String getContainerPublicCertFilename()
	{
		String ret = getProperty(CONTAINER_PUBLIC_CERT_FILENAME_PROPERTY_NAME);
		if (ret == null)
			throw new ConfigurationException(String.format(
				"Missing required bootstrap property \"%s\".",
				CONTAINER_PUBLIC_CERT_FILENAME_PROPERTY_NAME));
		return ret;
	}
}