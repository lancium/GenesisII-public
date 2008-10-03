package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
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
		StringOrPath arg;
		if (filesystemName == null)
			arg = new StringOrPath(argument);
		else
			arg = new StringOrPath(new FilesystemRelativePath(
				filesystemName, argument));
		
		((PosixLikeApplicationUnderstanding)currentUnderstanding).addArgument(
			arg);
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
			currentUnderstanding).setWorkingDirectory(
				new File(workingDirectory));
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
		String filesystemName, String environment) throws JSDLException
	{
		StringOrPath env;
		
		if (filesystemName == null)
			env = new StringOrPath(environment);
		else
			env = new StringOrPath(new FilesystemRelativePath(
				filesystemName, environment));
		
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).addEnvironment(name, env);
	}
	
	@Override
	public void consumeInput(Object currentUnderstanding,
		String filesystemName, String input) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdinRedirect(
				new FilesystemRelativePath(filesystemName, input));
	}
	
	@Override
	public void consumeOutput(Object currentUnderstanding,
		String filesystemName, String output) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdoutRedirect(
				new FilesystemRelativePath(filesystemName, output));
	}
	
	@Override
	public void consumeError(Object currentUnderstanding,
		String filesystemName, String error) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStderrRedirect(
				new FilesystemRelativePath(filesystemName, error));
	}
	
	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String filesystemName, String executable) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setExecutable(
				new FilesystemRelativePath(filesystemName, executable));
	}
}
