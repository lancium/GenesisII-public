package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultHPCApplicationFacet;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;

public class CommonPosixLikeHPCApplicationFacet extends
		DefaultHPCApplicationFacet
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
	public void consumeWorkingDirectory(Object currentUnderstanding,
		String workingDirectory) throws JSDLException
	{
		if (!BESUtilities.canOverrideBESWorkerDir())
			throw new UnsupportedJSDLElement(
				new QName(HPCConstants.HPC_NS, "WorkingDirectory"));
		
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setWorkingDirectory(workingDirectory);
	}
	
	@Override
	public void consumeArgument(Object currentUnderstanding,
		String argument) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)currentUnderstanding).addArgument(
			argument);
	}
	
	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
		String environment) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).addEnvironment(name, environment);
	}
	
	@Override
	public void consumeInput(Object currentUnderstanding,
		String input) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdinRedirect(input);
	}

	@Override
	public void consumeOutput(Object currentUnderstanding,
		String output) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStdoutRedirect(output);
	}

	@Override
	public void consumeError(Object currentUnderstanding,
		String error) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setStderrRedirect(error);
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String executable) throws JSDLException
	{
		((PosixLikeApplicationUnderstanding)
			currentUnderstanding).setExecutable(executable);
	}
}
