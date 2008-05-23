package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPOSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;

public class SEPOSIXApplicationFacet extends DefaultPOSIXApplicationFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
			throws JSDLException
	{
		return new ForkExecUnderstanding();
	}
	
	@Override
	public void completeFacet(Object parentUnderstanding,
			Object currentUnderstanding) throws JSDLException
	{
		SimpleExecutionUnderstanding parent =
			((SimpleExecutionUnderstanding)parentUnderstanding);
		ForkExecUnderstanding child =
			((ForkExecUnderstanding)currentUnderstanding);
		
		child.validate();
		parent.setApplication(child);
	}

	@Override
	public void consumeArgument(Object currentUnderstanding,
			String filesystemName, String argument) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).addArgument(argument);
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
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "WorkingDirectory"));
		
		((ForkExecUnderstanding)currentUnderstanding).setWorkingDirectory(
			workingDirectory);
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
			String filesystemName, String environment) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).addEnvironment(
			name, environment);
	}

	@Override
	public void consumeInput(Object currentUnderstanding,
			String filesystemName, String input) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).setStdinRedirect(input);
	}

	@Override
	public void consumeOutput(Object currentUnderstanding,
			String filesystemName, String output) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).setStdoutRedirect(output);
	}

	@Override
	public void consumeError(Object currentUnderstanding,
			String filesystemName, String error) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).setStderrRedirect(error);
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding,
			String filesystemName, String executable) throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(
				new QName(JSDLPosixConstants.JSDL_POSIX_NS, "filesystemName"));
		
		((ForkExecUnderstanding)currentUnderstanding).setExecutable(
			executable);
	}
}