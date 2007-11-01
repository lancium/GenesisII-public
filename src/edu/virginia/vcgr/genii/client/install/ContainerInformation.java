package edu.virginia.vcgr.genii.client.install;

import java.io.Serializable;
import java.net.URL;

public class ContainerInformation implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _deploymentName;
	private URL _containerURL;
	
	public ContainerInformation(String deploymentName, URL containerURL)
	{
		_deploymentName = deploymentName;
		_containerURL = containerURL;
	}
	
	public String getDeploymentName()
	{
		return _deploymentName;
	}
	
	public URL getContainerURL()
	{
		return _containerURL;
	}
	
	public String toString()
	{
		return _deploymentName;
	}
}
