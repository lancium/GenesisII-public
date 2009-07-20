package edu.virginia.vcgr.genii.client.jsdl.personality;

import java.net.URI;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface SPMDApplicationFacet extends PersonalityFacet
{
	public void consumeExecutable(
		Object currentUnderstanding, String filesystemName,
		String executable) throws JSDLException;
	public void consumeArgument(
		Object currentUnderstanding, String filesystemName,
		String argument) throws JSDLException;
	public void consumeInput(Object currentUnderstanding,
			String filesystemName, String input) throws JSDLException;
	public void consumeOutput(Object currentUnderstanding,
			String filesystemName, String output) throws JSDLException;
	public void consumeError(Object currentUnderstanding,
			String filesystemName, String error) throws JSDLException;
	public void consumeWorkingDirectory(
		Object currentUnderstanding, String filesystemName,
		String workingDirectory) throws JSDLException;
	public void consumeEnvironment(
		Object currentUnderstanding, String name, 
		String filesystemName, String environment) 
			throws JSDLException;
	public void consumeUserName(
		Object currentUnderstanding, String userName) 
			throws JSDLException;
	public void consumeNumberOfProcesses(
		Object currentUnderstanding, Integer numberOfProcesses,
		boolean useActualCPUCount) throws JSDLException;
	public void consumeProcessesPerHost(
		Object currentUnderstanding, Integer processesPerHost)
			throws JSDLException;
	public void consumeThreadsPerProcess(
		Object currentUnderstanding, Integer threadsPerProcess,
		boolean useActualIndividualCPUCount) throws JSDLException;
	public void consumeSPMDVariation(
		Object currentUnderstanding, URI spmdVariation) throws JSDLException;
}