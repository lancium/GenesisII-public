package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class CloudGenerateJobFilePhase implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	
	private String _workingDir;
	private String _jobFile;
	private JobRequest _job;
	
	public CloudGenerateJobFilePhase(String workingDir,
			String jobFile, JobRequest job){
		_workingDir = workingDir;
		_jobFile = jobFile;
		_job = job;
	}
	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"generating-job-file", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		FileOutputStream fOut = new FileOutputStream(_workingDir + _jobFile);
		ObjectOutputStream oOut = new ObjectOutputStream(fOut);
		oOut.writeObject(_job);
		oOut.close();
		fOut.close();
	}

}
