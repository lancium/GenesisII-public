package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Installation;

public class ContainerProperties extends Properties
{
	static final long serialVersionUID = 0L;

	static final private String CONTAINER_PROPERTIES_FILENAME =
		"container.properties";
	
	static final private String GENII_USER_DIR_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.user-dir";
	static final private String GENII_DEPLOYMENT_NAME_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.deployment-name";
	
	static public ContainerProperties containerProperties = 
		new ContainerProperties();
	
	static private File getContainerPropertiesFile()
	{
		File ret = new File(Installation.getInstallDirectory(), 
			CONTAINER_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		
		return null;
	}
	
	private boolean _existed = false;
	
	private ContainerProperties()
	{
		File file = getContainerPropertiesFile();
		if (file != null)
		{
			InputStream in = null;
			try
			{
				in = new FileInputStream(file);
				load(in);
				_existed = true;
			}
			catch (IOException e)
			{
				return;
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
	}
	
	public boolean existed()
	{
		return _existed;
	}
	
	public String getUserDirectory()
	{
		String val = getProperty(GENII_USER_DIR_PROPERTY_NAME);
		if ( (val == null) || val.equals(ApplicationBase.USER_DIR_PROPERTY_VALUE))
			val = ApplicationBase.getUserDirFromEnvironment();
		return val;
	}
	
	public String getDeploymentName()
	{
		return getProperty(GENII_DEPLOYMENT_NAME_PROPERTY_NAME);
	}
}