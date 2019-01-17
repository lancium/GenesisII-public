package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ApplicationUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.DataStagingUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ExecutionUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.JobUnderstandingContext;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ResourceConstraints;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CleanupPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CreateWorkingDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupContextDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupFUSEPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.SetupOGRSHPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageInPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StageOutPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StoreContextPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeardownFUSEPhase;

public class CommonExecutionUnderstanding implements ExecutionUnderstanding
{
	private FilesystemManager _fsManager;

	private String _jobAnnotation = null;
	private String _jobName = null;

	private Collection<DataStagingUnderstanding> _stageIns = new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _stageOuts = new LinkedList<DataStagingUnderstanding>();
	private Collection<DataStagingUnderstanding> _pureCleans = new LinkedList<DataStagingUnderstanding>();

	private String _requiredOGRSHVersion = null;
	private String _fuseDirectory = null;

	private Double _totalPhysicalMemory = null;
	private Double _wallclockTimeLimit = null;
	private Double _totalCPUCount = null;
	private Double _individualCPUCount = null;

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

		if ((source == null) && (target == null) && stage.isDeleteOnTerminate())
			_pureCleans.add(stage);
	}

	public void addFilesystem(FilesystemUnderstanding understanding) throws JSDLException
	{
		if (understanding.isScratchFileSystem()) {
			_fsManager.addFilesystem("SCRATCH", understanding.createScratchFilesystem(_jobAnnotation));
		} else if (understanding.isGridFileSystem()) {
			_fsManager.addFilesystem(understanding.getFileSystemName(), understanding.createGridFilesystem());
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

	public Double getWallclockTimeLimit()
	{
		return _wallclockTimeLimit;
	}

	public void setWallclockTimeLimit(Double wallclockTimeLimit)
	{
		_wallclockTimeLimit = wallclockTimeLimit;
	}

	public Double getTotalCPUCount()
	{
		return _totalCPUCount;
	}

	public void setTotalCPUCount(Double totalCPUCount)
	{
		_totalCPUCount = totalCPUCount;
	}

	public Double getIndividualCPUCount()
	{
		return _individualCPUCount;
	}

	public void setIndividualCPUCount(Double individualCPUCount)
	{
		_individualCPUCount = individualCPUCount;
	}

	final public Vector<ExecutionPhase> createExecutionPlan(BESConstructionParameters creationProperties) throws JSDLException
	{
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();

		if (MyProxyCertificate.isAvailable())
			createCertificateFileonDisk();

		ret.add(new CreateWorkingDirectoryPhase(getWorkingDirectory()));

		for (DataStagingUnderstanding stage : _stageIns) {
			File stageFile = _fsManager.lookup(stage.getFilePath());

			ret.add(new StageInPhase(stage.getSourceURI(), stageFile, stage.getCreationFlag(), stage.isHandleAsArchive(),
				stage.getUsernamePassword()));

			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stageFile));
		}

		ret.add(new SetupContextDirectoryPhase(".genesisII-bes-state"));
		cleanups.add(new CleanupPhase(new File(".genesisII-bes-state")));

		if (_fuseDirectory != null) {
			ret.add(new SetupFUSEPhase(_fuseDirectory));
			cleanups.add(new TeardownFUSEPhase(_fuseDirectory));
		}

		if (_requiredOGRSHVersion != null) {
			String storedOGRSHContextFilename = "stored-ogrsh-context.dat";
			String OGRSHConfigFilename = "ogrsh-config.xml";

			ret.add(new SetupOGRSHPhase(storedOGRSHContextFilename, OGRSHConfigFilename));
			ret.add(new StoreContextPhase(storedOGRSHContextFilename));

			cleanups.add(new CleanupPhase(new File(storedOGRSHContextFilename)));
			cleanups.add(new CleanupPhase(new File(OGRSHConfigFilename)));
		}

		ResourceConstraints resourceConstraints = new ResourceConstraints();
		resourceConstraints.setTotalPhysicalMemory(getTotalPhysicalMemory());
		resourceConstraints.setWallclockTimeLimit(getWallclockTimeLimit());

		// Check wallclock time and memory constraint
		if (creationProperties != null) {
			ResourceOverrides overrides = creationProperties.getResourceOverrides();
			if (overrides != null) {
				// check memory
				Size besUpperLimit = overrides.physicalMemory();
				Double requestedSize = resourceConstraints.getTotalPhysicalMemory();
				if (besUpperLimit != null && requestedSize != null) {
					if (requestedSize > besUpperLimit.as(SizeUnits.Bytes))
						throw new JSDLMatchException(
							String.format("Job requested %f bytes, but BES limits to %s.", requestedSize, besUpperLimit));
				}

				// Wallclock
				Duration besUpperWall = overrides.wallclockTimeLimit();
				Double requestedWall = resourceConstraints.getWallclockTimeLimit();
				if (besUpperWall != null && requestedWall != null) {
					if (requestedWall > besUpperWall.as(DurationUnits.Seconds))
						throw new JSDLMatchException(
							String.format("Job requested %f seconds, but BES limits to %s.", requestedWall, besUpperWall));
				}
			}
		}

		File fuseMountPoint = null;
		JSDLFileSystem gridFs = _fsManager.getGridFilesystem();
		if (gridFs != null)
			fuseMountPoint = gridFs.getMountPoint();

		JobUnderstandingContext jobContext = new JobUnderstandingContext(fuseMountPoint, resourceConstraints);

		if (_application != null)
			_application.addExecutionPhases(creationProperties, ret, cleanups, jobContext);

		for (DataStagingUnderstanding stage : _stageOuts) {
			File stageFile = _fsManager.lookup(stage.getFilePath());

			ret.add(new StageOutPhase(stageFile, stage.getTargetURI(), stage.isHandleAsArchive(), stage.isAlwaysStageOut(), stage.getUsernamePassword()));

			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stageFile));
		}
		if (MyProxyCertificate.isAvailable())
			cleanups.add(new CleanupPhase(new File(getWorkingDirectory().toString() + GenesisIIConstants.myproxyFilenameSuffix)));

		for (DataStagingUnderstanding stage : _pureCleans)
			cleanups.add(new CleanupPhase(_fsManager.lookup(stage.getFilePath())));

		ret.addAll(cleanups);
		return ret;
	}

	private void createCertificateFileonDisk()
	{
		try {
			FileWriter fstream = new FileWriter(getWorkingDirectory().toString() + GenesisIIConstants.myproxyFilenameSuffix);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(MyProxyCertificate.getPEMString());
			out.close();
		} catch (Exception e) {
		}

	}
}
