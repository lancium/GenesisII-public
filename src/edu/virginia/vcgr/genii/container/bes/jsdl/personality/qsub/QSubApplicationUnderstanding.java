package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.QueueProcessPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.JobUnderstandingContext;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.PosixLikeApplicationUnderstanding;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.StringOrPath;

class QSubApplicationUnderstanding 
	extends PosixLikeApplicationUnderstanding
{
    static private Log _logger = LogFactory.getLog(QSubApplicationUnderstanding.class);

	public QSubApplicationUnderstanding(FilesystemManager fsManager,
		BESWorkingDirectory workingDirectory)
	{
		super(fsManager, workingDirectory);
	}
	
	@Override
	public void addExecutionPhases(BESConstructionParameters creationProperties,
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, JobUnderstandingContext jobContext)
		throws JSDLException
	{
		File fuseMountPoint = jobContext.getFuseMountPoint();
		
		Deployment deployment = Installation.getDeployment(
			new DeploymentName());
		DeploymentName depName = deployment.getName();
		
		FilesystemManager fsManager = getFilesystemManager();
		executionPlan.add(new PrepareApplicationPhase(
			fsManager, getExecutable()));
		
		Map<String, StringOrPath> env = getEnvironment();
		env.put("GENII_DEPLOYMENT_NAME", new StringOrPath(depName.toString()));
		env.put("GENII_USER_DIR", new StringOrPath(".genesisII-bes-state"));
		_logger.info("rewrote GENII_USER_DIR to be: " + env.get("GENII_USER_DIR"));
		
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
		
		executionPlan.add(new QueueProcessPhase(
			fuseMountPoint,
			getSPMDVariation(), getNumProcesses(), 
			getNumProcessesPerHost(),
			fsManager.lookup(getExecutable()),
			stringArgs, stringEnv,
			fsManager.lookup(getStdinRedirect()),
			fsManager.lookup(getStdoutRedirect()),
			fsManager.lookup(getStderrRedirect()),
			creationProperties,
			jobContext.getResourceConstraints()));
	}
}
