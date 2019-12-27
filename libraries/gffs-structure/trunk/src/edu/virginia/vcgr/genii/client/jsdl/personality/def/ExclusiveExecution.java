package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface ExclusiveExecution {

	void consumeExclusiveExecution(Object currentUnderstanding, Boolean exclusiveExecution) throws JSDLException;

}
