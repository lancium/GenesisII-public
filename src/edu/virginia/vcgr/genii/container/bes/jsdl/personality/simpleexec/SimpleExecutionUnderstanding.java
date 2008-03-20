package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CleanupPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageInPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageOutPhase;

public class SimpleExecutionUnderstanding
{
	private String _jobName = null;
	
	private Collection<DataStagingUnderstanding> _stageIns =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _stageOuts =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _pureCleans =
		new LinkedList<DataStagingUnderstanding>();
	
	private Application _application = null;
	
	public void setJobName(String jobName)
	{
		_jobName = jobName;
	}
	
	public String getJobName()
	{
		return _jobName;
	}
	
	public void addDataStaging(DataStagingUnderstanding stage)
	{
		if (stage.getSourceURI() != null)
			_stageIns.add(stage);
		if (stage.getTargetURI() != null)
			_stageOuts.add(stage);
		
		if (stage.getSourceURI() == null && stage.getTargetURI() == null &&
			stage.isDeleteOnTerminate())
			_pureCleans.add(stage);
	}
	
	public void setApplication(Application application)
	{
		_application = application;
	}
	
	public Vector<ExecutionPhase> createExecutionPlan() throws JSDLException
	{
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();
		
		for (DataStagingUnderstanding stage : _stageIns)
		{
			ret.add(new StageInPhase(
				stage.getSourceURI(), stage.getFilename(), 
				stage.getCreationFlag()));
			
			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stage.getFilename()));
		}
		
		if (_application != null)
			_application.addExecutionPhases(ret, cleanups);
		
		for (DataStagingUnderstanding stage : _stageOuts)
		{
			ret.add(new StageOutPhase(
				stage.getFilename(), stage.getTargetURI()));
			
			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stage.getFilename()));
		}
		
		for (DataStagingUnderstanding stage : _pureCleans)
		{
			cleanups.add(new CleanupPhase(stage.getFilename()));
		}
		
		ret.addAll(cleanups);
		return ret;
	}
}