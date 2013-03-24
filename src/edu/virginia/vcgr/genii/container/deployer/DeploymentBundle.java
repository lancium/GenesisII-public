package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;
import java.io.Serializable;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;

public class DeploymentBundle implements IDeployment, Serializable
{
	static final long serialVersionUID = 0L;

	private IJSDLReifier _reifier;
	private File _targetDirectory;
	private String _deploymentInstance;

	DeploymentBundle(String deploymentInstance, File targetDirectory, IJSDLReifier reifier)
	{
		_reifier = reifier;
		_targetDirectory = targetDirectory;
		_deploymentInstance = deploymentInstance;
	}

	public JobDefinition_Type reifyJSDL(JobDefinition_Type jsdl) throws DeploymentException
	{
		return _reifier.reifyJSDL(_targetDirectory, jsdl);
	}

	public void terminate() throws DeploymentException
	{
		DeploymentManager.decrementCount(_deploymentInstance);
	}
}