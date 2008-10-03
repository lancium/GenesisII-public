package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.OGRSHVersion;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.QueueProcessPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.PosixLikeApplicationUnderstanding;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.StringOrPath;

class QSubApplicationUnderstanding 
	extends PosixLikeApplicationUnderstanding
{
	public QSubApplicationUnderstanding(FilesystemManager fsManager,
		BESWorkingDirectory workingDirectory)
	{
		super(fsManager, workingDirectory);
	}
	
	@Override
	public void addExecutionPhases(Properties creationProperties,
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, String ogrshVersion)
		throws JSDLException
	{
		Deployment deployment = Installation.getDeployment(
			new DeploymentName());
		DeploymentName depName = deployment.getName();
		
		FilesystemManager fsManager = getFilesystemManager();
		executionPlan.add(new PrepareApplicationPhase(
			fsManager.lookup(getExecutable())));
		
		Map<String, StringOrPath> env = getEnvironment();
		env.put("GENII_DEPLOYMENT_NAME", 
			new StringOrPath(depName.toString()));
		env.put("GENII_USER_DIR", 
			new StringOrPath(".genesisII-bes-state"));
		
		Map<String, String> stringEnv = new HashMap<String, String>();
		for (String key : env.keySet())
		{
			StringOrPath sop = env.get(key);
			stringEnv.put(key, sop.toString(fsManager));
		}
		
		Collection<String> stringArgs = new LinkedList<String>();
		for (StringOrPath sop : getArguments())
		{
			stringArgs.add(sop.toString(fsManager));
		}
		
		if (ogrshVersion == null)
		{
			executionPlan.add(new QueueProcessPhase(
				getSPMDVariation(), getNumProcesses(),
				fsManager.lookup(getExecutable()),
				stringArgs, stringEnv,
				fsManager.lookup(getStdinRedirect()),
				fsManager.lookup(getStdoutRedirect()),
				fsManager.lookup(getStderrRedirect()),
				creationProperties));
		} else
		{
			stringEnv.put("BES_HOME", "/home/bes-job");
			stringEnv.put("OGRSH_CONFIG", "./ogrsh-config.xml");
			stringEnv.put("GENII_USER_DIR", ".");
			
			Vector<String> args = new Vector<String>();
			args.add(fsManager.lookup(getExecutable()).getAbsolutePath());
			args.addAll(stringArgs);
			
			OGRSHVersion oVersion = Installation.getOGRSH(
				).getInstalledVersions().get(ogrshVersion);
			File shim = oVersion.shimScript();
			
			executionPlan.add(new QueueProcessPhase(
				getSPMDVariation(), getNumProcesses(),
				shim, args, stringEnv,
				fsManager.lookup(getStdinRedirect()),
				fsManager.lookup(getStdoutRedirect()),
				fsManager.lookup(getStderrRedirect()),
				creationProperties));
		}
	}
}