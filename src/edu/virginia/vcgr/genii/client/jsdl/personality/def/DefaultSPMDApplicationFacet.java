package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.SPMDApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.client.jsdl.spmd.SPMDConstants;

public class DefaultSPMDApplicationFacet extends DefaultPersonalityFacet
		implements SPMDApplicationFacet
{
	@Override
	public void consumeArgument(Object currentUnderstanding,
			String filesystemName, String argument)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Argument"));
	}

	@Override
	public void consumeEnvironment(Object currentUnderstanding,
			String name, String filesystemName,
			String environment) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Environment"));
	}

	@Override
	public void consumeError(Object currentUnderstanding,
			String filesystemName, String error) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Error"));
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding,
			String filesystemName, String executable)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Executable"));
	}

	@Override
	public void consumeInput(Object currentUnderstanding,
			String filesystemName, String input) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Input"));
	}

	@Override
	public void consumeOutput(Object currentUnderstanding,
			String filesystemName, String output)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Output"));
	}

	@Override
	public void consumeUserName(Object currentUnderstanding,
			String userName) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "UserName"));
	}

	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding,
			String filesystemName, String workingDirectory)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLPosixConstants.JSDL_POSIX_NS, "WorkingDirectory"));
	}

	@Override
	public void consumeNumberOfProcesses(Object currentUnderstanding,
			Integer numberOfProcesses, boolean useActualCPUCount)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(SPMDConstants.JSDL_SPMD_NS, "NumberOfProcesses"));
	}

	@Override
	public void consumeProcessesPerHost(Object currentUnderstanding,
			Integer processesPerHost) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(SPMDConstants.JSDL_SPMD_NS, "ProcessesPerHost"));
	}

	@Override
	public void consumeSPMDVariation(Object currentUnderstanding,
			URI spmdVariation) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(SPMDConstants.JSDL_SPMD_NS, "SPMDVariation"));
	}

	@Override
	public void consumeThreadsPerProcess(Object currentUnderstanding,
			Integer threadsPerProcess, boolean useActualIndividualCPUCount)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(SPMDConstants.JSDL_SPMD_NS, "ThreadsPerProcess"));
	}
}