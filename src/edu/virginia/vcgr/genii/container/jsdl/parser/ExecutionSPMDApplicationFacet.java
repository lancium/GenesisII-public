package edu.virginia.vcgr.genii.container.jsdl.parser;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultSPMDApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.spmd.SPMDConstants;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemRelative;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;
import edu.virginia.vcgr.genii.container.jsdl.SPMDInformation;

public class ExecutionSPMDApplicationFacet extends DefaultSPMDApplicationFacet
{
	private int _numberOfProcesses = 1;
	private String _spmdVariation = null;
	
	@Override
	public void consumeNumberOfProcesses(Object currentUnderstanding,
		Integer numberOfProcesses, boolean useActualCPUCount)
			throws JSDLException
	{
		if (useActualCPUCount)
			throw new UnsupportedJSDLElement(
				new QName(SPMDConstants.JSDL_SPMD_NS, "useActualCPUCount"));
		
		_numberOfProcesses = numberOfProcesses.intValue();
	}
	
	@Override
	public void consumeSPMDVariation(Object currentUnderstanding,
		URI spmdVariation)
	{
		_spmdVariation = spmdVariation.toString();
	}
	
	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String filesystemName, String executable)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setExecutable(new FilesystemRelative<String>(
			filesystemName, executable));
	}
	
	@Override
	public void consumeArgument(Object currentUnderstanding,
		String filesystemName, String argument)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.addArgument(new FilesystemRelative<String>(
			filesystemName, argument));
	}
	
	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding,
		String filesystemName, String workingDirectory)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setWorkingDirectory(new FilesystemRelative<String>(
			filesystemName, workingDirectory));
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding,
		String name, String filesystemName, String environment)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.addEnvironmentVariable(name, new FilesystemRelative<String>(
			filesystemName, environment));
	}
	
	@Override
	public void consumeInput(Object currentUnderstanding,
		String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStdinRedirect(new FilesystemRelative<String>(
			filesystemName, redirect));
	}

	@Override
	public void consumeOutput(Object currentUnderstanding,
		String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStdoutRedirect(new FilesystemRelative<String>(
			filesystemName, redirect));
	}

	@Override
	public void consumeError(Object currentUnderstanding,
		String filesystemName, String redirect)
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setStderrRedirect(new FilesystemRelative<String>(
			filesystemName, redirect));
	}

	@Override
	public void completeFacet(Object parentUnderstanding,
			Object currentUnderstanding) throws JSDLException
	{
		JobRequest jr = (JobRequest)currentUnderstanding;
		jr.setSPMDInformation(new SPMDInformation(
			_spmdVariation, _numberOfProcesses));
	}
}