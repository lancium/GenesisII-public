package edu.virginia.vcgr.genii.container.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultHPCApplicationFacet;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionHPCApplicationFacet extends DefaultHPCApplicationFacet
{
	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding,
		String workingDirectory)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setWorkingDirectory(new FilesystemRelative<String>(
			null, workingDirectory));
	}
	
	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String executable)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setExecutable(new FilesystemRelative<String>(null, executable));
	}
	
	@Override
	public void consumeArgument(Object currentUnderstanding,
		String argument)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.addArgument(new FilesystemRelative<String>(null, argument));
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding, 
		String name, String environment)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.addEnvironmentVariable(name, new FilesystemRelative<String>(
			null, environment));
	}
	
	@Override
	public void consumeInput(Object currentUnderstanding,
		String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStdinRedirect(new FilesystemRelative<String>(null, redirect));
	}
	
	@Override
	public void consumeOutput(Object currentUnderstanding,
		String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStdoutRedirect(new FilesystemRelative<String>(null, redirect));
	}
	
	@Override
	public void consumeError(Object currentUnderstanding,
		String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStderrRedirect(new FilesystemRelative<String>(null, redirect));
	}
}