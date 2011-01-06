package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudJobWrapper;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class CloudGenerateRunScriptPhase  implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	
	private String _workingDir;
	private JobRequest _job;
	private String _resourceFile;
	private String _scratchDir;
	private String _runScript;
	
	static private Log _logger = 
		LogFactory.getLog(CloudGenerateRunScriptPhase.class);
	
	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"generating-wrapper-scripts", false);
	}
	
	public CloudGenerateRunScriptPhase(String scratchDir, String runScript,
			String workingDir, String resourceFile, JobRequest job){
		_scratchDir = scratchDir;
		_runScript = runScript;
		_workingDir = workingDir;
		_resourceFile = resourceFile;
		_job = job;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		try
		{

			FileOutputStream tStream = 
				new FileOutputStream(_scratchDir + _runScript);
			CloudJobWrapper.generateWrapperScript(tStream,
					new File(_workingDir),
					new File(_workingDir + _resourceFile),
					_job, new File(_scratchDir));
			tStream.close();
	
		}
		catch (Exception e)
		{
			_logger.error(e);
		}	
		
	}

}
