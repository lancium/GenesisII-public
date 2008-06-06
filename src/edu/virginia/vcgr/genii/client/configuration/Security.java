package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class Security
{
	static private Log _logger = LogFactory.getLog(Security.class);
	
	static private final String SECURITY_DIRECTORY_NAME = "security";
	static private final String SECURITY_PROPERTIES_FILE_NAME = 
		"security.properties";
	
	private File _securityDirectory;
	private File _securityPropertiesFile;
	private Properties _securityProperties;
	
	Security(File deploymentDirectory, File configurationDirectory)
	{
		_securityDirectory = new File(deploymentDirectory, 
			SECURITY_DIRECTORY_NAME);
		_securityPropertiesFile = new File(configurationDirectory, 
			SECURITY_PROPERTIES_FILE_NAME);
		_securityProperties = new Properties();
		
		if (!_securityDirectory.exists())
			throw new InvalidDeploymentException(deploymentDirectory.getName(),
				"Couldn't find security directory in deployment.");
		if (!_securityPropertiesFile.exists())
			throw new InvalidDeploymentException(deploymentDirectory.getName(),
				"Couldn't find security properties file \"" +
				SECURITY_PROPERTIES_FILE_NAME + 
				" in deployment's configuration directory.");
		
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(_securityPropertiesFile);
			_securityProperties.load(fin);
		}
		catch (IOException ioe)
		{
			_logger.fatal(
				"Unable to load security properties from deployment.", ioe);
			throw new InvalidDeploymentException(deploymentDirectory.getName(),
				"Unable to load security properties from deployment.");
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	public File getSecurityFile(String filename)
	{
		return new File(_securityDirectory, filename);
	}
	
	public String getProperty(String propertyName)
	{
		return getProperty(propertyName, null);
	}
	
	public String getProperty(String propertyName, String def)
	{
		return _securityProperties.getProperty(propertyName, def);
	}
}