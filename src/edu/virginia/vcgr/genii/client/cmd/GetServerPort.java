package edu.virginia.vcgr.genii.client.cmd;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.container.configuration.ContainerConfiguration;

public class GetServerPort
{
	static private final String _USAGE = "Usage: GetServerPort <deployment name>";

	static private void printUsage()
	{
		System.out.print(_USAGE + "\n");
	}
	
	static public int getServerPort(String deployName)
	{
		System.setProperty(GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY, deployName);
		String userDir = GetUserDir.getUserDir();
		ConfigurationManager configurationManager = 
			ConfigurationManager.initializeConfiguration(userDir);
		ContainerConfiguration serverConf = new ContainerConfiguration(configurationManager);
		
		return serverConf.getListenPort();
	}
	
	static public void main(String [] args)
	{
		if (args.length != 1)
		{
			printUsage();
			return;
		}
		String deployName = args[0];
		int port = getServerPort(deployName);
		System.out.print(port + "\n");
	}
}