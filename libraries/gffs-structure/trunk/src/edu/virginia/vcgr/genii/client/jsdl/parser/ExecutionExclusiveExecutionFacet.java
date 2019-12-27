package edu.virginia.vcgr.genii.client.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.Restrictions;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultExclusiveExecutionFacet;

public class ExecutionExclusiveExecutionFacet extends DefaultExclusiveExecutionFacet
{
	@Override
	public void consumeExclusiveExecution(Object currentUnderstanding, Boolean exclusiveExecution) throws JSDLException
	{
		Restrictions restrictions = (Restrictions) currentUnderstanding;
		restrictions.setExclusiveExecutionRestriction(exclusiveExecution);
	}
}