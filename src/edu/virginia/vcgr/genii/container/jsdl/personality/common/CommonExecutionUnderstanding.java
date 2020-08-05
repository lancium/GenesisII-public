package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.client.jsdl.JSDLMatchException;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ApplicationUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.DataStagingUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ExecutionUnderstanding;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.JobUnderstandingContext;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ResourceConstraints;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CheckBinariesPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CleanupPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.CompleteAccountingPhase;
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
	static private Log _logger = LogFactory.getLog(CommonExecutionUnderstanding.class);

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

	private Double _GPUCountPerNode = null;
 	private Double _GPUMemoryPerNode = null;

	private ApplicationUnderstanding _application = null;
	
	private JSDLFileSystem _scratchFS = null;

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
	
	public JSDLFileSystem rememberScratch(JSDLFileSystem newScratchDir) {
		if (_scratchFS != null) {
			_logger.error("scratch fs dir was already set and is being overwritten; approach is not workable.");
		}
		_scratchFS = newScratchDir;
		return newScratchDir;
	}

	public void addFilesystem(FilesystemUnderstanding understanding) throws JSDLException
	{
		if (understanding.isScratchFileSystem()) {
			// 2020-07-11 ASG. the parameter "_jobAnnotation does not seem right.
			_fsManager.addFilesystem("SCRATCH", rememberScratch(understanding.createScratchFilesystem(_jobAnnotation)));
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

	public Double getGPUCountPerNode()
     	{
 		return _GPUCountPerNode;
     	}
 
 	public void setGPUCountPerNode(Double GPUCountPerNode)
     	{
 		_GPUCountPerNode = GPUCountPerNode;
     	}
 
 	public Double getGPUMemoryPerNode()
     	{
 		return _GPUMemoryPerNode;
     	}
 
 	public void setGPUMemoryPerNode(Double GPUMemoryPerNode)
     	{
 		_GPUMemoryPerNode = GPUMemoryPerNode;
     	}

	final public Vector<ExecutionPhase> createExecutionPlan(BESConstructionParameters creationProperties, JobDefinition_Type jsdl) throws JSDLException
	{
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();
		// 2020-07-10 by ASG
		// A few things to note. Once the createActivity is called the activity is off to the races, so anything we want in place
		// ahead of that must be done first. In particular, we will set up the accounting directory and 

		JobDescription_Type jobDes=jsdl.getJobDescription(0);
		if (jobDes!=null) {
			JobIdentification_Type jobID=jobDes.getJobIdentification();
			if (jobID!=null) {
				String []annotations=jobID.getJobAnnotation();
				if (annotations!=null) {
					String portalID=jobID.getJobAnnotation(0);
					_jobAnnotation=portalID;
					if (portalID!=null ) System.out.println("Portal id is " + portalID);
				}
			}
		}
		// End of extracting jobAnnotation[0]
		
		if (MyProxyCertificate.isAvailable())
			createCertificateFileonDisk();
		
		File scratchPath = null;
		if (_scratchFS != null) {
			scratchPath = _scratchFS.getMountPoint();
			// pre-remove the scratch link before the working dir cleanup hits it.
			cleanups.add(new CleanupPhase(new File(getWorkingDirectory().getWorkingDirectory().toString(), "scratch")));
		}			
		_logger.info("scratch path found for working dir to use is: " + scratchPath);
		ret.add(new CreateWorkingDirectoryPhase(getWorkingDirectory(), scratchPath));

		for (DataStagingUnderstanding stage : _stageIns) {
			File stageFile = _fsManager.lookup(stage.getFilePath());

			ret.add(new StageInPhase(stage.getSourceURI(), stageFile, stage.getCreationFlag(), stage.isHandleAsArchive(),
				stage.getUsernamePassword()));

			if (stage.isDeleteOnTerminate())
				cleanups.add(new CleanupPhase(stageFile));
		}
		
		// CCH 2020-06-29: Check that images in local filesystem are sufficiently up-to-date
		// Assumptions:
		// Images in GFFS are either missing or completely stable (i.e. no partially written files)
		// Images in GFFS are stored at /home/CCC/Lancium/<username>/Images/
		// Images on local filesystem are stored under <JWD>/../Images/<username>
		// The plan is to create a phase that will stage in newer images from the GFFS if the local FS is outdated
		
		FilesystemRelativePath execPath = _application.getExecutable();
		String execName =  execPath.getString();
		if (execName.endsWith(".qcow2") || execName.endsWith(".sif") || execName.endsWith(".simg")) {
			ret.add(new CheckBinariesPhase(execName));
		}
		// CCH 2020-06-29, end of new code

		ret.add(new SetupContextDirectoryPhase(".genesisII-bes-state"));
		//CAK 2019-05-30: below was a bug before, where directory path was not included.
		cleanups.add(new CleanupPhase(new File(getWorkingDirectory().getWorkingDirectory().toString(), ".genesisII-bes-state")));

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
		if (getTotalPhysicalMemory()!=null) resourceConstraints.setTotalPhysicalMemory(getTotalPhysicalMemory());
		resourceConstraints.setWallclockTimeLimit(getWallclockTimeLimit());
		resourceConstraints.setGPUCountPerNode(getGPUCountPerNode());
 		resourceConstraints.setGPUMemoryPerNode(getGPUMemoryPerNode());
 		// 2020-04-21 by ASG. Deep in the coronovirus.
 		// We need a default number of CPUs .. actually VCPUs
 		if (resourceConstraints.getTotalCPUCount()==null) {
 			resourceConstraints.setTotalCPUCount(new Double(1.0));
 			setTotalCPUCount(new Double(1.0));
 		}
 		if (resourceConstraints.getTotalPhysicalMemory()==null) {
 			resourceConstraints.setTotalPhysicalMemory(new Double(2.0*1024.0*1024.0*1024.0*resourceConstraints.getTotalCPUCount())); // Set to 2GB per CPU .. note these are VCPUs
 			setTotalPhysicalMemory(new Double(2.0*1024.0*1024.0*1024.0*resourceConstraints.getTotalCPUCount()));
 		}


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

				// gpus per node
				Integer gpusperNode = overrides.gpuCount();
 				Double requestedGpus = resourceConstraints.getGPUCountPerNode();
 				_logger.info("--------JSDL:------ in commonExecutionUnderstanding.java----" + gpusperNode + "---" +requestedGpus);
 				if (gpusperNode != null && requestedGpus != null) {
 					if (requestedGpus.intValue() > gpusperNode)
 						throw new JSDLMatchException(
 							String.format("Job requested %f gpus, but BES limits to %d.", requestedGpus, gpusperNode));
 				}
 
 				// check gpu memory per node
 				Size gpuMemoryUpperLimit = overrides.gpuMemoryPerNode();
 				Double requestedGPUMemSize = resourceConstraints.getGPUMemoryPerNode();
 				_logger.info("-----JSDL:----- in commonExecutionUnderstanding.java----" + gpuMemoryUpperLimit + "---" + requestedGPUMemSize);
 				if (gpuMemoryUpperLimit != null && requestedGPUMemSize != null) {
 					if (requestedGPUMemSize > gpuMemoryUpperLimit.as(SizeUnits.Bytes))
 						throw new JSDLMatchException(
 							String.format("Job requested %f bytes, but BES limits to %s.", requestedGPUMemSize, gpuMemoryUpperLimit));
 				}
			}
		}
		
		File fuseMountPoint = null;
		JSDLFileSystem gridFs = _fsManager.getGridFilesystem();
		if (gridFs != null)
			fuseMountPoint = gridFs.getMountPoint();
		String jobName = getJobName();
		JobUnderstandingContext jobContext = new JobUnderstandingContext(fuseMountPoint, resourceConstraints, jobName);

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

		// 2020-05-29 by ASG
		// Adding code to move the accounting directory to "Accounting/finished"
		File f=getWorkingDirectory().getWorkingDirectory();
		String JWD=f.getName();
		String sharedDir=f.getParent();
		File accountingDirectory= new File(sharedDir+"/Accounting/"+JWD);
		File finishedDir=new File(sharedDir+"/Accounting/finished/" + JWD);
		// CompleteAccountingPhase(File accountingDirectory, File finishedDir)
		ret.add(new CompleteAccountingPhase(accountingDirectory,finishedDir));
		// End of accounting dir updates
		
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
