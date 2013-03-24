package edu.virginia.vcgr.genii.container.deployer;

public class DeploymentInformation
{
	private String _instanceID;
	private String _directoryName;

	public DeploymentInformation(String instanceID, String directoryName)
	{
		_instanceID = instanceID;
		_directoryName = directoryName;
	}

	public String getInstanceID()
	{
		return _instanceID;
	}

	public String getDirectoryName()
	{
		return _directoryName;
	}
}