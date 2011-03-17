package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.ggf.jsdl.JobDefinition_Type;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.cloud.CloudJobWrapper;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;
import edu.virginia.vcgr.genii.container.jsdl.parser.ExecutionProvider;
import edu.virginia.vcgr.genii.context.ContextType;

public class RunJSDL extends BaseGridTool{

	private String _type = "jsdl";
	
	static private final String _DESCRIPTION =
		"Runs a grid job locally, will not run if working directory exists";
	static private final String _USAGE =
		"runJSDL <workingDir> <JobFile> [--type=<jsdl|binary>]";

	@Option({"type"})
	public void setType(String type) {
		_type = type;
	}
	
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

	
		InputStream in = null;

		File wDir = new File(getArgument(0));
		GeniiPath source = new GeniiPath(getArgument(1));

		if (!source.exists())
			throw new FileNotFoundException(String.format(
					"Unable to find source file %s!", source));
		if (!source.isFile())
			throw new IOException(String.format(
					"Source path %s is not a file!", source));

		in = source.openInputStream();

		JobRequest tJob = null;

		if(_type.equals("jsdl")){
			JobDefinition_Type jsdl =
				(JobDefinition_Type)ObjectDeserializer.deserialize(
						new InputSource(in), JobDefinition_Type.class);
			PersonalityProvider provider = new ExecutionProvider();
			tJob = (JobRequest)JSDLInterpreter.interpretJSDL(provider, jsdl);
			in.close();
		}
		else if(_type.equals("binary")){
			ObjectInputStream oIn = new ObjectInputStream(in);
			tJob = (JobRequest)oIn.readObject();
			in.close();
		}
		else{
			stdout.println("Invalid input type");
			return 0;
		}
		
		
		StageDataTool stageTool;
		//Create Working directory, 
		//will only run if working directory does not exist
		if(wDir.mkdir()){

			//Generate bash script
			stdout.println("Generating Pwrapper Script");
			File resourceUsage = 
				new File(getArgument(0) + "/resourceusage.xml");
			
			File submitScript = 
				File.createTempFile("exec", ".sh", wDir);
			OutputStream ps = new FileOutputStream(submitScript);
			
			CloudJobWrapper.generateWrapperScript(
					ps, wDir, resourceUsage, tJob, wDir);
			ps.close();

			//Stage in
			stdout.println("Staging in");
			stageTool = new StageDataTool();
			stageTool.addArgument(getArgument(0));
			stageTool.addArgument(getArgument(1));
			stageTool.setDirection("in");
			stageTool.run(stdout, stderr, stdin);
			
			//Execute
			stdout.println("Executing");
			submitScript.setExecutable(true, true);
			new File(wDir.getAbsolutePath() + "/" + 
					tJob.getExecutable().getTarget()).setExecutable(true, true);
			Runtime.getRuntime().exec(submitScript.getAbsolutePath()).waitFor();

			//Stage Out
			stdout.println("Staging Out");
			stageTool.setDirection("out");
			stageTool.run(stdout, stderr, stdin);
			
			
			//Complete
			stdout.println("Job Executed");
			
		}
		else
			stdout.println("Working directory must not already exist");
		
		return 0;
	}



	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}

}
