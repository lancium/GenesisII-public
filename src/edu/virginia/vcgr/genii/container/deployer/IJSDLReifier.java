package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;

public interface IJSDLReifier
{
	public JobDefinition_Type reifyJSDL(File deployDirectory,
		JobDefinition_Type jobDef) throws DeploymentException;
}