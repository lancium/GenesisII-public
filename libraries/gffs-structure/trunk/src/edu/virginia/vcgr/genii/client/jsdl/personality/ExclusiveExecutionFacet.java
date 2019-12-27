package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface ExclusiveExecutionFacet extends PersonalityFacet
{
	public void consumeExclusiveExecution(Object currentUnderstanding, Boolean exclusiveExecution) throws JSDLException;
}
