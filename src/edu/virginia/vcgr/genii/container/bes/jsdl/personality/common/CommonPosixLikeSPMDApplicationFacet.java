package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultSPMDApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.client.jsdl.spmd.SPMDConstants;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;

public class CommonPosixLikeSPMDApplicationFacet 
	extends DefaultSPMDApplicationFacet
{
	@Override
	public void consumeNumberOfProcesses(Object currentUnderstanding,
			Integer numberOfProcesses, boolean useActualCPUCount)
			throws JSDLException
	{
		if (useActualCPUCount)
			throw new UnsupportedJSDLElement(
				new QName(SPMDConstants.JSDL_SPMD_NS, "useActualCPUCount"));
		
		((PosixLikeApplicationUnderstanding)currentUnderstanding).setNumProcesses(
			numberOfProcesses.intValue());
	}

	@Override
	public void consumeSPMDVariation(Object currentUnderstanding,
			URI spmdVariation) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)currentUnderstanding).setSPMDVariation(
			spmdVariation);
	}

	@Override
	public void completeFacet(Object parentUnderstanding,
		Object currentUnderstanding) throws JSDLException
	{
		CommonExecutionUnderstanding parent =
			((CommonExecutionUnderstanding)parentUnderstanding);
		PosixLikeApplicationUnderstanding child =
			((PosixLikeApplicationUnderstanding)currentUnderstanding);
		
		child.validate();
		parent.setApplication(child);
	}
	
	@Override
	public void consumeArgument (Object currentUnderstanding,
		String filesystemName, String argument) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((PosixLikeApplicationUnderstanding)currentUnderstanding).addArgument(
			argument);
	}
	
	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding,
		String filesystemName, String workingDirectory)
		throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		if (!BESUtilities.canOverrideBESWorkerDir())
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, 
					"WorkingDirectory"));
		
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setWorkingDirectory(workingDirectory);
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
		String filesystemName, String environment) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
			
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).addEnvironment(name, environment);
	}
	
	@Override
	public void consumeInput(Object currentUnderstanding,
		String filesystemName, String input) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
			
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdinRedirect(input);
	}
	
	@Override
	public void consumeOutput(Object currentUnderstanding,
		String filesystemName, String output) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
			
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdoutRedirect(output);
	}
	
	@Override
	public void consumeError(Object currentUnderstanding,
		String filesystemName, String error) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
			
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStderrRedirect(error);
	}
	
	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String filesystemName, String executable) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
			
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setExecutable(executable);
	}
}