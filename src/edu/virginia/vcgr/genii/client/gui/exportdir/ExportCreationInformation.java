package edu.virginia.vcgr.genii.client.gui.exportdir;

import edu.virginia.vcgr.genii.client.install.ContainerInformation;

public class ExportCreationInformation
{
	private boolean _isLightWeight;
	private ContainerInformation _containerInfo;
	private String _localPath;
	private String _rnsPath;
	
	public ExportCreationInformation(ContainerInformation containerInfo,
		String localPath, String rnsPath, boolean isLightWeight)
	{
		_containerInfo = containerInfo;
		_localPath = localPath;
		_rnsPath = rnsPath;
		_isLightWeight = isLightWeight;
	}
	
	public boolean isLightWeight()
	{
		return _isLightWeight;
	}
	
	public ContainerInformation getContainerInformation()
	{
		return _containerInfo;
	}
	
	public String getLocalPath()
	{
		return _localPath;
	}
	
	public String getRNSPath()
	{
		return _rnsPath;
	}
}