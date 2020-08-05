package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;

public class CloudGenerateJobFilePhase implements ExecutionPhase, Serializable
{

	static final long serialVersionUID = 0L;

	private String _workingDir;
	private String _jobFile;
	private JobRequest _job;

	public CloudGenerateJobFilePhase(String workingDir, String jobFile, JobRequest job)
	{
		_workingDir = workingDir;
		_jobFile = jobFile;
		_job = job;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "generating-job-file", false);
	}

	@Override
	public void execute(ExecutionContext context, Object activityObject) throws Throwable
	{
		BESActivity activity = (BESActivity) activityObject;
		FileOutputStream fOut = new FileOutputStream(_workingDir + _jobFile);
		ObjectOutputStream oOut = new ObjectOutputStream(fOut);
		oOut.writeObject(_job);
		oOut.close();
		fOut.close();
	}

}
