package edu.virginia.vcgr.genii.container.deployer;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;

public interface IDeployment
{
	public void terminate() throws DeploymentException;
	
	public JobDefinition_Type reifyJSDL(JobDefinition_Type jsdl)
		throws DeploymentException;
}