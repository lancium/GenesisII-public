package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.ggf.jsdl.JobDefinition_Type;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;
import edu.virginia.vcgr.genii.container.jsdl.parser.ExecutionProvider;
import edu.virginia.vcgr.genii.context.ContextType;

public class RunJSDL extends BaseGridTool
{

	private String _type = "jsdl";

	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/drunJSDL";
	static private final String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/urunJSDL";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/runJSDL";

	@Option({ "type" })
	public void setType(String type)
	{
		_type = type;
	}

	public RunJSDL()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}

		InputStream in = null;

		File wDir = new File(getArgument(0));
		GeniiPath source = new GeniiPath(getArgument(1));

		if (!source.exists())
			throw new FileNotFoundException(String.format("Unable to find source file %s!", source));
		if (!source.isFile())
			throw new IOException(String.format("Source path %s is not a file!", source));

		in = source.openInputStream();

		JobRequest tJob = null;

		if (_type.equals("jsdl")) {
			JobDefinition_Type jsdl = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(in),
				JobDefinition_Type.class);
			PersonalityProvider provider = new ExecutionProvider();
			tJob = (JobRequest) JSDLInterpreter.interpretJSDL(provider, jsdl);
			in.close();
		} else if (_type.equals("binary")) {
			ObjectInputStream oIn = new ObjectInputStream(in);
			tJob = (JobRequest) oIn.readObject();
			in.close();
		} else {
			stdout.println("Invalid input type");
			return 0;
		}

		StageDataTool stageTool;
		// Create Working directory,
		// will only run if working directory does not exist
		if (wDir.mkdir()) {

			// Generate bash script
			stdout.println("Generating Pwrapper Script");
			File resourceUsage = new File(getArgument(0) + "/resourceusage.xml");

			File submitScript = File.createTempFile("exec", ".sh", wDir);
			OutputStream ps = new FileOutputStream(submitScript);

			generateWrapperScript(ps, wDir, resourceUsage, tJob, wDir);
			ps.close();

			// Stage in
			stdout.println("Staging in");
			stageTool = new StageDataTool();
			stageTool.addArgument(getArgument(0));
			stageTool.addArgument(getArgument(1));
			stageTool.setDirection("in");
			stageTool.run(stdout, stderr, stdin);

			// Execute
			stdout.println("Executing");
			submitScript.setExecutable(true, true);
			new File(wDir.getAbsolutePath() + "/" + tJob.getExecutable().getTarget()).setExecutable(true, true);
			Runtime.getRuntime().exec(submitScript.getAbsolutePath()).waitFor();

			// Stage Out
			stdout.println("Staging Out");
			stageTool.setDirection("out");
			stageTool.run(stdout, stderr, stdin);

			// Complete
			stdout.println("Job Executed");

		} else
			stdout.println("Working directory must not already exist");

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}

	private static void generateWrapperScript(OutputStream tStream, File workingDir, File resourceUsage, JobRequest job,
		File tmpDir) throws Exception
	{
		try {

			PrintStream ps = new PrintStream(tStream);

			// Generate Header
			ps.format("#!%s\n\n", "/bin/bash");

			// Generate App Body
			ps.format("cd \"%s\"\n", workingDir.getAbsolutePath());

			ResourceOverrides overrides = new ResourceOverrides();

			ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(tmpDir, overrides.operatingSystemName(),
				overrides.cpuArchitecture());

			boolean first = true;

			String execName = job.getExecutable().getTarget();
			if (!execName.contains("/"))
				execName = String.format("./%s", execName);

			for (String element : wrapper.formCommandLine(
				null,
				null, // app.getEnvironment()
				workingDir, getRedirect(job.getStdinRedirect(), workingDir), getRedirect(job.getStdoutRedirect(), workingDir),
				getRedirect(job.getStderrRedirect(), workingDir), resourceUsage, execName,
				getArguments(new String[job.getArguments().size()], job.getArguments())))

			{
				if (!first)
					ps.format(" ");
				first = false;
				if (element.contains(tmpDir.getAbsolutePath())) {
					element = workingDir.getAbsolutePath() + element.substring(element.lastIndexOf("/"));
				}
				ps.format("\"%s\"", element);
			}
			ps.println();
			// Generate complete file
			ps.println("touch executePhase.complete");
			ps.flush();
		} catch (Exception e) {
			throw e;
		}
	}

	private static File getRedirect(FilesystemRelative<String> tPath, File workingDir)
	{
		if (tPath == null)
			return null;
		return new File(workingDir.toString() + "/" + tPath.getTarget());
	}

	private static String[] getArguments(String[] args, List<FilesystemRelative<String>> tArgs)
	{
		int i = 0;
		for (FilesystemRelative<String> tArg : tArgs) {
			args[i] = tArg.getTarget();
			i++;
		}
		return args;
	}

}
