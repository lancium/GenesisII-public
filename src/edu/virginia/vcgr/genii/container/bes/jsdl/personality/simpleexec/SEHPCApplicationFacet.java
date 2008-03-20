package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultHPCApplicationFacet;

public class SEHPCApplicationFacet extends DefaultHPCApplicationFacet
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
		String argument) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).addArgument(argument);
	}

	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
		String environment) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).addEnvironment(
			name, environment);
	}

	@Override
	public void consumeInput(Object currentUnderstanding,
		String input) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).setStdinRedirect(input);
	}

	@Override
	public void consumeOutput(Object currentUnderstanding,
		String output) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).setStdoutRedirect(output);
	}

	@Override
	public void consumeError(Object currentUnderstanding,
		String error) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).setStderrRedirect(error);
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding,
		String executable) throws JSDLException
	{
		((ForkExecUnderstanding)currentUnderstanding).setExecutable(
			executable);
	}
}