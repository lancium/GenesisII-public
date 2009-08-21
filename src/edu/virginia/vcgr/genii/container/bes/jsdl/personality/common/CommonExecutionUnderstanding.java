package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CleanupPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CreateWorkingDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupContextDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupFUSEPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupOGRSHPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageInPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageOutPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StoreContextPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeardownFUSEPhase;

public class CommonExecutionUnderstanding
{
	private FilesystemManager _fsManager;
	
	private String _jobAnnotation = null;
	private String _jobName = null;
	
	private Collection<DataStagingUnderstanding> _stageIns =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _stageOuts =
		new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _pureCleans =
		new LinkedList<DataStagingUnderstanding>();
	
	private String _requiredOGRSHVersion = null;
	private String _fuseDirectory = null;
	
	private Double _totalPhysicalMemory = null;
	
	private ApplicationUnderstanding _application = null;
	
	public CommonExecutionUnderstanding(FilesystemManager fsManager)
	{
		_fsManager = fsManager;
	}
	
	public void setJobAnnotation(String jobAnnotation)
	{
		_jobAnnotation = jobAnnotation;
	}
	
	public void setJobName(String jobName)
	{
		_jobName = jobName;
	}
	
	public String getJobAnnotation()
	{
		return _jobAnnotation;
	}
	
	public String getJobName()
	{
		return _jobName;
	}
	
	public void setFuseMountDirectory(String fuseDirectory)
	{
		_fuseDirectory = fuseDirectory;
	}
	
	public String getFuseMountDirectory()
	{
		return _fuseDirectory;
	}
	
	public void addDataStaging(DataStagingUnderstanding stage)
	{
		URI source = stage.getSourceURI();
		URI target = stage.getTargetURI();
		
		if (source != null)
			_stageIns.add(stage);
		if (target != null)
			_stageOuts.add(stage);
		
		if ( (source == null) && (target == null) && 
			stage.isDeleteOnTerminate())
			_pureCleans.add(stage);
	}
	
	public void addFilesystem(FilesystemUnderstanding understanding)
		throws JSDLException
	{
		if (understanding.isScratchFileSystem())
		{
			_fsManager.addFilesystem("SCRATCH", 
				understanding.createScratchFilesystem(_jobAnnotation));
		} else if (understanding.isGridFileSystem())
		{
			_fsManager.addFilesystem(understanding.getFileSystemName(),
				understanding.createGridFilesystem(
					new File(getWorkingDirectory().getWorkingDirectory(), 
						"grid-mnt")));
		}
	}
	
	public void setApplication(ApplicationUnderstanding application)
	{
		_application = application;
	}
	
	public void setRequiredOGRSHVersion(String version)
	{
		_requiredOGRSHVersion = version;
	}
	
	public String getRequiredOGRSHVersion()
	{
		return _requiredOGRSHVersion;
	}
	
	public BESWorkingDirectory getWorkingDirectory()
	{
		if (_application == null)
			return null;
		
		return _application.getWorkingDirectory();
	}
	
	public Double getTotalPhysicalMemory()
	{
		return _totalPhysicalMemory;
	}
	
	public void setTotalPhysicalMemory(Double totalPhysicalMemory)
	{
		_totalPhysicalMemory = totalPhysicalMemory;
	}
	
	public Vector<ExecutionPhase> createExecutionPlan(
		Properties creationProperties) throws JSDLException
	{
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();
		
		ret.add(new CreateWorkingDirectoryPhase(getWorkingDirectory()));
		
		for (DataStagingUnderstanding stage : _stageIns)
		{
			File stageFile = _fsManager.lookup(stage.getFilePath());
			
			ret.add(new StageInPhase(
				stage.getSourceURI(), 
				stageFile,
				stage.getCreationFlag(), stage.getCredential()));
			
			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stageFile));
		}
		
		ret.add(new SetupContextDirectoryPhase(".genesisII-bes-state"));
		cleanups.add(new CleanupPhase(new File(".genesisII-bes-state")));
		
		if (_fuseDirectory != null)
		{
			ret.add(new SetupFUSEPhase(_fuseDirectory));
			cleanups.add(new TeardownFUSEPhase(_fuseDirectory));
		}
		
		if (_requiredOGRSHVersion != null)
		{
			String storedOGRSHContextFilename = "stored-ogrsh-context.dat";
			String OGRSHConfigFilename = "ogrsh-config.xml";
			
			ret.add(new SetupOGRSHPhase(
				storedOGRSHContextFilename, OGRSHConfigFilename));
			ret.add(new StoreContextPhase(storedOGRSHContextFilename));
			
			cleanups.add(new CleanupPhase(
				new File(storedOGRSHContextFilename)));
			cleanups.add(new CleanupPhase(
				new File(OGRSHConfigFilename)));
		}
		
		JobUnderstandingContext jobContext = new JobUnderstandingContext(_requiredOGRSHVersion, getTotalPhysicalMemory());
		
		if (_application != null)
			_application.addExecutionPhases(
				creationProperties, ret, cleanups, jobContext);
		
		for (DataStagingUnderstanding stage : _stageOuts)
		{
			File stageFile = _fsManager.lookup(stage.getFilePath());
			
			ret.add(new StageOutPhase(
				stageFile, stage.getTargetURI(),
				stage.getCredential()));
			
			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stageFile));
		}
		
		for (DataStagingUnderstanding stage : _pureCleans)
			cleanups.add(new CleanupPhase(
				_fsManager.lookup(stage.getFilePath())));
		
		ret.addAll(cleanups);
		return ret;
	}
}