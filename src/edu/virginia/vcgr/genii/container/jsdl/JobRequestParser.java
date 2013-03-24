package edu.virginia.vcgr.genii.container.jsdl;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.container.jsdl.parser.ExecutionProvider;

public class JobRequestParser
{
	static public JobRequest parse(JobDefinition_Type jsdl) throws JSDLException
	{
		PersonalityProvider provider = new ExecutionProvider();
		return (JobRequest) JSDLInterpreter.interpretJSDL(provider, jsdl);
	}
}