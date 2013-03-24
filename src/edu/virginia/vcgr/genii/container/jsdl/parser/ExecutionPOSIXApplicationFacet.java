package edu.virginia.vcgr.genii.container.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPOSIXApplicationFacet;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionPOSIXApplicationFacet extends DefaultPOSIXApplicationFacet
{
	@Override
	public void consumeExecutable(Object currentUnderstanding, String filesystemName, String executable)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setExecutable(new FilesystemRelative<String>(filesystemName, executable));
	}

	@Override
	public void consumeArgument(Object currentUnderstanding, String filesystemName, String argument)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.addArgument(new FilesystemRelative<String>(filesystemName, argument));
	}

	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding, String filesystemName, String workingDirectory)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setWorkingDirectory(new FilesystemRelative<String>(filesystemName, workingDirectory));
	}

	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name, String filesystemName, String environment)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.addEnvironmentVariable(name, new FilesystemRelative<String>(filesystemName, environment));
	}

	@Override
	public void consumeInput(Object currentUnderstanding, String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setStdinRedirect(new FilesystemRelative<String>(filesystemName, redirect));
	}

	@Override
	public void consumeOutput(Object currentUnderstanding, String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setStdoutRedirect(new FilesystemRelative<String>(filesystemName, redirect));
	}

	@Override
	public void consumeError(Object currentUnderstanding, String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest) currentUnderstanding;
		jr.setStderrRedirect(new FilesystemRelative<String>(filesystemName, redirect));
	}
}