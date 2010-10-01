package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ggf.jsdl.JobDefinition_Type;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.jsdl.BaseJob;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonExecutionUnderstanding;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.PosixLikeApplicationUnderstanding;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.forkexec.ForkExecPersonalityProvider;
import edu.virginia.vcgr.genii.context.ContextType;

public class RunJSDL extends BaseGridTool{


	static private final String _DESCRIPTION =
		"Runs a grid job locally, will not run if working directory exists";
	static private final String _USAGE =
		"runJSDL <workingDir> <JSDLFile>";

	public RunJSDL()
	{
		super(_DESCRIPTION, _USAGE, false);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		File wDir = new File(getArgument(0));


		GeniiPath source = new GeniiPath(getArgument(1));
		if (!source.exists())
			throw new FileNotFoundException(String.format(
					"Unable to find JSDL file %s!", source));
		if (!source.isFile())
			throw new IOException(String.format(
					"JSDL path %s is not a file!", source));
		InputStream in = source.openInputStream();

		JobDefinition_Type jsdl =
			(JobDefinition_Type)ObjectDeserializer.deserialize(new InputSource(in), JobDefinition_Type.class);

		stdout.println("Parsing JSDL");


		CommonExecutionUnderstanding understanding = (CommonExecutionUnderstanding)JSDLInterpreter.interpretJSDL(
				new ForkExecPersonalityProvider(new FilesystemManager(), new BESWorkingDirectory(wDir,false)), jsdl);
		BaseJob  tJob = understanding.generateBaseJob();


		//Create Working directory, will only run if working directory does not exist
		if(wDir.mkdir()){

			//Generate bash script
			System.out.println("Generating Pwrapper Script");
			File resourceUsage = new File(getArgument(0) + "/resourceusage.xml");
			PosixLikeApplicationUnderstanding tApp = (PosixLikeApplicationUnderstanding)understanding.getApplicationUnderstanding();

			File submitScript = File.createTempFile("exec", ".sh", wDir);
			OutputStream ps = new FileOutputStream(submitScript);
			tJob.generateJobScript(ps, wDir, resourceUsage, tApp);
			ps.close();

			//Stage in
			System.out.println("Staging in");
			tJob.stageIn(getArgument(0));

			//Execute
			System.out.println("Executing");
			submitScript.setExecutable(true, true);
			new File(wDir.getAbsolutePath() + "/" + tJob.get_exec()).setExecutable(true, true);
			Runtime.getRuntime().exec(submitScript.getAbsolutePath()).waitFor();

			//Stage Out
			System.out.println("Staging Out");
			tJob.stageOut(getArgument(0));
			stdout.println("Job Executed");
			
		}
		return 0;
	}



	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}

}
