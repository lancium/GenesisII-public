package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CleanupPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupContextDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupFUSEPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupOGRSHPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageInPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageOutPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StoreContextPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeardownFUSEPhase;

public class SimpleExecutionUnderstanding
{
	private String _jobName = null;
	
	private Collection<DataStagingUnderstanding> _stageIns =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _stageOuts =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _pureCleans =
		new LinkedList<DataStagingUnderstanding>();
	
	private String _requiredOGRSHVersion = null;
	private String _fuseDirectory = null;
	
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
	
	public void setRequiredOGRSHVersion(String version)
	{
		_requiredOGRSHVersion = version;
	}
	
	public void setFuseDirectory(String fuseDirectory)
	{
		_fuseDirectory = fuseDirectory;
	}
	
	public String getWorkingDirectory()
	{
		return _application.getWorkingDirectory();
	}
	
	public Vector<ExecutionPhase> createExecutionPlan(
		Properties creationProperties) throws JSDLException
	{
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();
		
		for (DataStagingUnderstanding stage : _stageIns)
		{
			ret.add(new StageInPhase(
				stage.getSourceURI(), stage.getFilename(), 
				stage.getCreationFlag(), stage.getCredential()));
			
			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stage.getFilename()));
		}
		
		ret.add(new SetupContextDirectoryPhase(".genesisII-bes-state"));
		cleanups.add(new CleanupPhase(".genesisII-bes-state"));
		
		if (_fuseDirectory != null)
		{
			ret.add(new SetupFUSEPhase(_fuseDirectory));
			cleanups.add(new TeardownFUSEPhase(_fuseDirectory));
		}
		
		if (_requiredOGRSHVersion != null)
		{
			String storedOGRSHContextFilename = "stored-ogrsh-context.dat";
			String OGRSHConfigFilename = "ogrsh-config.xml";
			ret.add(new SetupOGRSHPhase(storedOGRSHContextFilename, OGRSHConfigFilename));
			ret.add(new StoreContextPhase(storedOGRSHContextFilename));
			cleanups.add(new CleanupPhase(storedOGRSHContextFilename));
			cleanups.add(new CleanupPhase(OGRSHConfigFilename));
		}
		
		if (_application != null)
			_application.addExecutionPhases(creationProperties,
				ret, cleanups, _requiredOGRSHVersion);
		
		for (DataStagingUnderstanding stage : _stageOuts)
		{
			ret.add(new StageOutPhase(
				stage.getFilename(), stage.getTargetURI(),
				stage.getCredential()));
			
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
