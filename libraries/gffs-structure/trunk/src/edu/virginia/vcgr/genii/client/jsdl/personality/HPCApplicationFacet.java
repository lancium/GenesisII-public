package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface HPCApplicationFacet extends PersonalityFacet
{
	public void consumeName(Object currentUnderstanding, String name) throws JSDLException;

	public void consumeExecutable(Object currentUnderstanding, String executable) throws JSDLException;

	public void consumeArgument(Object currentUnderstanding, String argument) throws JSDLException;

	public void consumeInput(Object currentUnderstanding, String input) throws JSDLException;

	public void consumeOutput(Object currentUnderstanding, String output) throws JSDLException;

	public void consumeError(Object currentUnderstanding, String error) throws JSDLException;

	public void consumeWorkingDirectory(Object currentUnderstanding, String workingDirectory) throws JSDLException;

	public void consumeEnvironment(Object currentUnderstanding, String name, String value) throws JSDLException;

	public void consumeUserName(Object currentUnderstanding, String userName) throws JSDLException;
}