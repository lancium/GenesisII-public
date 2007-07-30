package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;

public interface IDeployerProvider
{
	public IJSDLReifier getReifier();
	public void deployApplication(File targetDirectory) 
		throws DeploymentException;
}