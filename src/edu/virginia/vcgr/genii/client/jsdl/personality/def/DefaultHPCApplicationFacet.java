package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;

public class DefaultHPCApplicationFacet extends DefaultPersonalityFacet
		implements HPCApplicationFacet
{
	@Override
	public void consumeArgument(Object currentUnderstanding, String argument)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "Argument"));
	}

	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name,
			String value) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "Environment"));
	}

	@Override
	public void consumeError(Object currentUnderstanding, String error)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
				new QName(HPCConstants.HPC_NS, "Error"));
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding, String executable)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "Executable"));
	}

	@Override
	public void consumeInput(Object currentUnderstanding, String input)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "Input"));
	}

	@Override
	public void consumeName(Object currentUnderstanding, String name)
			throws JSDLException
	{
	}

	@Override
	public void consumeOutput(Object currentUnderstanding, String output)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "Output"));
	}

	@Override
	public void consumeUserName(Object currentUnderstanding, String userName)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "UserName"));
	}

	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding,
			String workingDirectory) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(HPCConstants.HPC_NS, "WorkingDirectory"));
	}
}
