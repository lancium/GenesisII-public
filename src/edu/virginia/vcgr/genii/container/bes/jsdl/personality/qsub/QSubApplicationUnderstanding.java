package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.OGRSHVersion;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.QueueProcessPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.PosixLikeApplicationUnderstanding;

class QSubApplicationUnderstanding 
	extends PosixLikeApplicationUnderstanding
{
	@Override
	public void addExecutionPhases(Properties creationProperties,
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, String ogrshVersion)
		throws JSDLException
	{
		Deployment deployment = Installation.getDeployment(
			new DeploymentName());
		DeploymentName depName = deployment.getName();
		
		executionPlan.add(new PrepareApplicationPhase(getExecutable()));
		
		Map<String, String> env = getEnvironment();
		env.put("GENII_DEPLOYMENT_NAME", depName.toString());
		env.put("GENII_USER_DIR", ".genesisII-bes-state");
		
		if (ogrshVersion == null)
		{
			executionPlan.add(new QueueProcessPhase(
				getExecutable(), getArguments(), env,
				getStdinRedirect(), getStdoutRedirect(), getStderrRedirect(),
				creationProperties));
		} else
		{
			env.put("BES_HOME", "/home/bes-job");
			env.put("OGRSH_CONFIG", "./ogrsh-config.xml");
			env.put("GENII_USER_DIR", ".");
			
			Vector<String> args = new Vector<String>();
			args.add(getExecutable());
			args.addAll(getArguments());
			
			OGRSHVersion oVersion = Installation.getOGRSH(
				).getInstalledVersions().get(ogrshVersion);
			File shim = oVersion.shimScript();
			
			executionPlan.add(new QueueProcessPhase(
				shim.getAbsolutePath(), args, env,
				getStdinRedirect(), getStdoutRedirect(), getStderrRedirect(),
				creationProperties));
		}
	}
}