package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudJobWrapper;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class CloudGenerateRunScriptPhase implements ExecutionPhase, Serializable
{

	static final long serialVersionUID = 0L;

	private String _workingDir;
	private JobRequest _job;
	private String _resourceFile;
	private String _scratchDir;
	private String _runScript;
	private String _stageInScript;
	private String _stageOutScript;
	private String _genState;
	private String _jobFile;
	private String _genDir;
	private CmdLineManipulatorConfiguration _manipulatorConfiguration;

	static private Log _logger = LogFactory.getLog(CloudGenerateRunScriptPhase.class);

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "generating-wrapper-scripts", false);
	}

	public CloudGenerateRunScriptPhase(String scratchDir, String runScript, String workingDir, String resourceFile,
		JobRequest job, String stageInScript, String stageOutScript, String genState, String jobFile, String genDir,
		CmdLineManipulatorConfiguration manipulatorConfiguration)
	{
		_scratchDir = scratchDir;
		_runScript = runScript;
		_workingDir = workingDir;
		_resourceFile = resourceFile;
		_job = job;
		_stageInScript = stageInScript;
		_stageOutScript = stageOutScript;
		_genState = genState;
		_jobFile = jobFile;
		_genDir = genDir;
		_manipulatorConfiguration = manipulatorConfiguration;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		try {

			FileOutputStream tStream = new FileOutputStream(_scratchDir + _runScript);
			CloudJobWrapper.generateWrapperScript(tStream, new File(_workingDir), new File(_workingDir + _resourceFile), _job,
				new File(_scratchDir), _manipulatorConfiguration);
			tStream.close();

			// Generate Stage in and out scripts
			tStream = new FileOutputStream(_scratchDir + _stageInScript);
			PrintStream ps = new PrintStream(tStream);
			// Generate Header
			ps.format("#!%s\n\n", "/bin/bash");
			// Generate App Body
			ps.println("export GENII_USER_DIR=" + _workingDir + _genState);
			ps.println(_genDir + "grid stageData --direction=\"in\" " + "--type=\"binary\" " + _workingDir + " local:"
				+ _workingDir + _jobFile);
			ps.println("touch " + _workingDir + "stageInPhase.complete");
			ps.close();
			tStream.close();

			// Generate Stage in and out scripts
			tStream = new FileOutputStream(_scratchDir + _stageOutScript);
			ps = new PrintStream(tStream);
			// Generate Header
			ps.format("#!%s\n\n", "/bin/bash");
			// Generate App Body
			ps.println("export GENII_USER_DIR=" + _workingDir + _genState);
			ps.println(_genDir + "grid stageData --direction=\"out\" " + "--type=\"binary\" " + _workingDir + " local:"
				+ _workingDir + _jobFile);
			ps.println("touch " + _workingDir + "stageOutPhase.complete");
			ps.close();
			tStream.close();

		} catch (Exception e) {
			_logger.error(e);
		}

	}

}
