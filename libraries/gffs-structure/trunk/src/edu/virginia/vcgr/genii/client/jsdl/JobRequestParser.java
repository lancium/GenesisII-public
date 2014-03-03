package edu.virginia.vcgr.genii.client.jsdl;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.jsdl.parser.ExecutionProvider;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;

public class JobRequestParser {
	static public JobRequest parse(JobDefinition_Type jsdl)
			throws JSDLException {
		PersonalityProvider provider = new ExecutionProvider();
		return (JobRequest) JSDLInterpreter.interpretJSDL(provider, jsdl);
	}
}