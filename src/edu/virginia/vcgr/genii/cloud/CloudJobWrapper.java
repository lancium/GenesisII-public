package edu.virginia.vcgr.genii.cloud;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.cmdLineManipulator.CmdLineManipulatorUtils;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudCheckStatusPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudCopyDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudExecutePhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudGenerateJobFilePhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudGenerateRunScriptPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudGetResourcePhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudProcessAccountingPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudReleaseResourcePhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudSetPermissionsPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudSetupContextDirectoryPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudStageInPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.cloud.CloudStageOutPhase;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class CloudJobWrapper {

	
	static private Log _logger = LogFactory.getLog(CloudJobWrapper.class);
	
	public static void generateWrapperScript(OutputStream tStream,
			File workingDir, File resourceUsage, JobRequest job, File tmpDir,
			CmdLineManipulatorConfiguration manipulatorConfiguration) throws Exception{
		try
		{

			PrintStream ps = new PrintStream(tStream);

			//Generate Header
			ps.format("#!%s\n\n", "/bin/bash");

			//Generate App Body
			ps.format("cd \"%s\"\n", workingDir.getAbsolutePath());


			ResourceOverrides overrides = new ResourceOverrides();

			
			
			ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(
					tmpDir, overrides.operatingSystemName(),
					overrides.cpuArchitecture());
			boolean first = true;

			String execName = job.getExecutable().getTarget();
			if (!execName.contains("/"))
				execName = String.format("./%s", execName);
			
				
				//assemble job properties for cmdLineManipulators
				Map<String, Object> jobProperties = new HashMap<String, Object>();
				CmdLineManipulatorUtils.addBasicJobProperties(jobProperties, 
						execName, getArguments(getArguments(new String[job.getArguments().size()],
								job.getArguments())));
				CmdLineManipulatorUtils.addEnvProperties(jobProperties,
						null,
						null, workingDir, 
						getRedirect(job.getStdinRedirect(), workingDir), 
						getRedirect(job.getStdoutRedirect(), workingDir),
						getRedirect(job.getStderrRedirect(), workingDir), 
						resourceUsage,
						wrapper.getPathToWrapper());
			/*
			 * 	//Add for MPI, taken from JSDL
				CmdLineManipulatorUtils.addSPMDJobProperties(jobProperties, 
						application.getSPMDVariation(), 
						application.getNumProcesses(), 
						application.getNumProcessesPerHost());		
			*/
			
				List<String> newCmdLine = new Vector<String>();
				_logger.debug("Trying to call cmdLine manipulators.");
				try{
					newCmdLine = CmdLineManipulatorUtils.callCmdLineManipulators(
						jobProperties, manipulatorConfiguration);
				}
				catch(CmdLineManipulatorException execption){
					throw new NativeQueueException(String.format("CmdLine Manipulators failed: %s", 
							execption.getMessage()));
				}
				
				for (String element : newCmdLine)
				{
					if (!first)
						ps.format(" ");
					first = false;
					if (element.contains(tmpDir.getAbsolutePath())){
						element = workingDir.getAbsolutePath() +
						element.substring(element.lastIndexOf("/"));
					}
					ps.format("\"%s\"", element);
				}
		
	
			ps.println();
			//Generate complete file
			ps.println("touch executePhase.complete");
			ps.flush();
		}
		catch (Exception e)
		{
			_logger.error(e);
			throw e;
		}
	}
	
	private static File getRedirect(FilesystemRelative<String> tPath,
			File workingDir){
		if (tPath == null)
			return null;
		return new File(workingDir.toString() +
				"/" + tPath.getTarget());
	}


	public static Vector<ExecutionPhase> createExecutionPlan(
			String activityID, String besid,
			JobRequest job, BESConstructionParameters constructionParameters){
		
		
		Vector<ExecutionPhase> ret = new Vector<ExecutionPhase>();
		Vector<ExecutionPhase> cleanups = new Vector<ExecutionPhase>();

		CloudConfiguration cConfig = 
			constructionParameters.getCloudConfiguration();
		
		//Config
		String scratchDir = cConfig.getLocalScratchDir() + activityID + "/" ;
		String genState = ".genState";
		String remoteDir = cConfig.getRemoteScratchDir() + activityID + "/";
		String jobFile = "jobFile";
		String runScript = "runScript.sh";
		String resourceFile = "resourceFile";
		String stageInFile = "stageIn.sh";
		String stageOutFile = "stageOut.sh";

		//Create state files
		ret.add(new CloudSetupContextDirectoryPhase(scratchDir + genState));
		
		//Create state files
		ret.add(new CloudGenerateJobFilePhase(scratchDir, jobFile, job));

		//Get Resource
		ret.add(new CloudGetResourcePhase(activityID, besid));
		
		//Generate runScript
		ret.add(new CloudGenerateRunScriptPhase(scratchDir, runScript,
				remoteDir, resourceFile, job, stageInFile, stageOutFile,
				genState, jobFile, cConfig.getRemoteClientDir(),
				constructionParameters.getCmdLineManipulatorConfiguration()));
	
		//Move local scratch to remote scratch
		ret.add(new CloudCopyDirectoryPhase(scratchDir, remoteDir,
				activityID, besid));
		
		//Create List of files to be set to executable
		ArrayList<String> permList = new ArrayList<String>();
		permList.add(remoteDir + stageInFile);
		permList.add(remoteDir + stageOutFile);

		//Set permissions phase
		ret.add(new CloudSetPermissionsPhase(besid, activityID, permList));

		//Stage In Phase
		ret.add(new CloudStageInPhase(activityID, besid, remoteDir,
				stageInFile));
		
		//Stage In phase Poller
		ret.add(new CloudCheckStatusPhase(activityID, besid, remoteDir,
				"stageInPhase.complete", "stage-in"));
		
		//Create List of files to be set to executable
		permList = new ArrayList<String>();
		permList.add(remoteDir + runScript);
		permList.add(remoteDir + job.getExecutable().getTarget());
		permList.add(remoteDir + "pwrapper-linux-32"); //Fix to get name
		
		//Set permissions phase
		ret.add(new CloudSetPermissionsPhase(besid, activityID, permList));
		
		//Execution Phase
		ret.add(new CloudExecutePhase(activityID, besid,
				remoteDir, runScript));

		//Execution phase Poller
		ret.add(new CloudCheckStatusPhase(activityID, besid, remoteDir,
				"executePhase.complete", "execution"));
		
		//Stage Out Phase
		ret.add(new CloudStageOutPhase(activityID, besid, remoteDir,
				"stageOut.sh"));
		
		//Stage Out phase Poller
		ret.add(new CloudCheckStatusPhase(activityID, besid, remoteDir,
				"stageOutPhase.complete", "stage-out"));
		
		//Get Accounting Back (Phase 5)
		ArrayList<String> commandLine = new ArrayList<String>();
		ret.add(new CloudProcessAccountingPhase(activityID, besid,
				remoteDir + resourceFile, scratchDir + resourceFile, commandLine,
				constructionParameters));
		
		
		//Release Resource 
		ret.add(new CloudReleaseResourcePhase(activityID, besid));

		
		ret.addAll(cleanups);
		return ret;
	}
	
	private static String[] getArguments(String[] args,  List<FilesystemRelative<String>> tArgs){
		int i = 0;
		for (FilesystemRelative<String> tArg : tArgs){
			args[i] =  tArg.getTarget();
			i++;
		}
		return args;
	}
	
	private static Collection<String> getArguments(String [] tArgs){
		Collection<String> tStrings = new ArrayList<String>();
		for (String tArg : tArgs){
			tStrings.add(tArg);
		}
		
		return tStrings;
	}
	
}
