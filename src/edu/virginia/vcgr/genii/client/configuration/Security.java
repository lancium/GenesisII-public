package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlClientTool;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public class Security
{
	static private Log _logger = LogFactory.getLog(Security.class);
	
	static private final String SECURITY_DIRECTORY_NAME = "security";
	static private final String SECURITY_PROPERTIES_FILE_NAME = 
		"security.properties";
	static private final String ADMIN_CERTIFICATE_FILE =
		"administrator.cer";
	
	static private boolean _loadedAdministrator = false;
	static private Identity _administrator = null;
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
	
	public Identity getAdminIdentity()
	{
		synchronized(Security.class)
		{
			if (!_loadedAdministrator)
			{
				_loadedAdministrator = true;
				File file = getSecurityFile(ADMIN_CERTIFICATE_FILE);
				if (file.exists())
				{
					try
					{
						_administrator = GamlClientTool.downloadIdentity(
							file.getAbsolutePath(),
							true);
					}
					catch (Throwable cause)
					{
						_logger.warn(
							"Unable to get administrator certificate.", cause);
					}
				}
			}
			
			return _administrator;
		}
	}
	
	public boolean isDeploymentAdministrator(ICallingContext callingContext)
	{
		Identity adminIdentity = getAdminIdentity();
		
		if (adminIdentity == null)
			return false;
		
		try
		{
			for (Identity id : SecurityUtils.getCallerIdentities(callingContext))
			{
				if (adminIdentity.equals(id))
					return true;
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to determine if caller is admin.", cause);
		}
		
		return false;
	}
	
	static public boolean isAdministrator(ICallingContext callingContext) 
	{
		DeploymentName depName = new DeploymentName();
		return Installation.getDeployment(depName).security(
			).isDeploymentAdministrator(callingContext);
	}
	
	static public boolean isAdministrator()
	{
		try
		{
			return isAdministrator(ContextManager.getCurrentContext());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to determine if caller is admin.", cause);
			return false;
		}
	}
}