package edu.virginia.vcgr.genii.cloud;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
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
			File workingDir, File resourceUsage, JobRequest job, File tmpDir){
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

			for (String element : wrapper.formCommandLine(
					null, //app.getEnvironment()
					workingDir,
					getRedirect(job.getStdinRedirect(), workingDir), 
					getRedirect(job.getStdoutRedirect(), workingDir),
					getRedirect(job.getStderrRedirect(), workingDir),
					resourceUsage,
					"./" + job.getExecutable().getTarget(),
					job.getArguments().toArray(
							new String[job.getArguments().size()])))
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

		//Create state files
		ret.add(new CloudSetupContextDirectoryPhase(scratchDir + genState));
		
		//Create state files
		ret.add(new CloudGenerateJobFilePhase(scratchDir, jobFile, job));

		//Get Resource
		ret.add(new CloudGetResourcePhase(activityID, besid));
		
		//Generate runScript
		ret.add(new CloudGenerateRunScriptPhase(scratchDir, runScript,
				remoteDir, resourceFile, job, "stageIn.sh", "stageOut.sh",
				genState, jobFile, cConfig.getRemoteClientDir()));
	
		//Move local scratch to remote scratch
		ret.add(new CloudCopyDirectoryPhase(scratchDir, remoteDir,
				activityID, besid));
		
		//Create List of files to be set to executable
		ArrayList<String> permList = new ArrayList<String>();
		permList.add(remoteDir + "stageIn.sh");
		permList.add(remoteDir + "stageOut.sh");

		//Set permissions phase
		ret.add(new CloudSetPermissionsPhase(besid, activityID, permList));

		//Stage In Phase
		ret.add(new CloudStageInPhase(activityID, besid, remoteDir,
				"stageIn.sh"));
		
		//Stage In phase Poller
		ret.add(new CloudCheckStatusPhase(activityID, besid, remoteDir,
				"stageInPhase.complete", "Stage In"));
		
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
				"executePhase.complete", "Execution"));
		
		//Stage Out Phase
		ret.add(new CloudStageOutPhase(activityID, besid, remoteDir,
				"stageOut.sh"));
		
		//Stage Out phase Poller
		ret.add(new CloudCheckStatusPhase(activityID, besid, remoteDir,
				"stageOutPhase.complete", "Stage Out"));
		
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
	
}
