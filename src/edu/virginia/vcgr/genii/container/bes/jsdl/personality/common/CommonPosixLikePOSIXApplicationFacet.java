package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPOSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;

public class CommonPosixLikePOSIXApplicationFacet extends
		DefaultPOSIXApplicationFacet
{
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
