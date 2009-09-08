package edu.virginia.vcgr.genii.ui;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.utils.io.IOUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class UIConfiguration
{
	static final private String UI_PROPERTIES_FILENAME =
		"ui.properties";
	
	static final private String ERROR_REPORT_TARGET_PROPERTY =
		"edu.virginia.vcgr.genii.ui.error.report-target";
	static final private String DEFAULT_ERROR_REPORT_TARGET =
		"http://vcgr.cs.virginia.edu/ui/report/reporter.php";
	
	private Properties _uiProperties = null;
	
	public UIConfiguration(DeploymentName deploymentName)
	{
		try
		{
			File propertiesFile = Installation.getDeployment(
				deploymentName).getConfigurationFile(UI_PROPERTIES_FILENAME);
			_uiProperties = IOUtils.loadProperties(propertiesFile);
		}
		catch (Throwable cause)
		{
			throw new ConfigurationException(
				"Unable to load UI properties file.", cause);
		}
	}
	
	public UIConfiguration()
	{
		this(new DeploymentName());
	}
	
	public URI errorReportTarget()
	{
		return URI.create(_uiProperties.getProperty(
			ERROR_REPORT_TARGET_PROPERTY, DEFAULT_ERROR_REPORT_TARGET));
	}
}