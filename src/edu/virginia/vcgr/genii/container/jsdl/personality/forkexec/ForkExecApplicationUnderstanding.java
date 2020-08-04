package edu.virginia.vcgr.genii.container.jsdl.personality.forkexec;

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
import edu.virginia.vcgr.genii.client.jsdl.DirBasedGridFileSystem;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityServiceImpl;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PassiveStreamRedirectionDescription;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.RunProcessPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupFUSEPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeardownFUSEPhase;
import edu.virginia.vcgr.genii.container.jsdl.personality.common.PosixLikeApplicationUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.JobUnderstandingContext;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.StringOrPath;

class ForkExecApplicationUnderstanding extends PosixLikeApplicationUnderstanding
{
	static private Log _logger = LogFactory.getLog(ForkExecApplicationUnderstanding.class);

	public ForkExecApplicationUnderstanding(FilesystemManager fsManager, BESWorkingDirectory workingDirectory)
	{
		super(fsManager, workingDirectory);
	}

	@Override
	public void addExecutionPhases(BESConstructionParameters creationProperties, Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, JobUnderstandingContext jobContext) throws JSDLException
	{
		File fuseMountPoint = jobContext.getFuseMountPoint();

		PassiveStreamRedirectionDescription redirection = getStreamRedirectionDescription();

		Deployment deployment = Installation.getDeployment(new DeploymentName());
		DeploymentName depName = deployment.getName();

		FilesystemManager fsManager = getFilesystemManager();

		for (JSDLFileSystem fs : fsManager.getFileSystems()) {
			if (fs instanceof DirBasedGridFileSystem) {
				DirBasedGridFileSystem gfs = (DirBasedGridFileSystem) fs;
				executionPlan.add(new SetupFUSEPhase(gfs.getMountPoint().getAbsolutePath(), gfs.getSandbox()));
				cleanupPhases.add(new TeardownFUSEPhase(gfs.getMountPoint().getAbsolutePath()));
			}
		}

		executionPlan.add(new PrepareApplicationPhase(fsManager, getExecutable()));
		Map<String, StringOrPath> env = getEnvironment();
		env.put("GENII_DEPLOYMENT_NAME", new StringOrPath(depName.toString()));
		env.put("GENII_USER_DIR", new StringOrPath(".genesisII-bes-state"));
		_logger.info("rewrote GENII_USER_DIR to be: " + env.get("GENII_USER_DIR").toString(fsManager));

		Map<String, String> stringEnv = new HashMap<String, String>();
		for (String key : env.keySet()) {
			StringOrPath sop = env.get(key);
			stringEnv.put(key, sop.toString(fsManager));
		}

		Collection<String> stringArgs = new LinkedList<String>();
		for (StringOrPath sop : getArguments()) {
			stringArgs.add(sop.toString(fsManager));
		}

		executionPlan.add(new RunProcessPhase(fuseMountPoint, getSPMDVariation(),getMemory(), getNumProcesses(), getNumProcessesPerHost(),
			getThreadsPerProcess(), BESActivityServiceImpl.getCommonDirectory(creationProperties), fsManager.lookup(getExecutable()),
			stringArgs.toArray(new String[0]), stringEnv, redirection, creationProperties));
	}
}
