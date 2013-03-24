package edu.virginia.vcgr.genii.container.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionResourcesFacet extends DefaultResourcesFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding) throws JSDLException
	{
		JobRequest request = (JobRequest) parentUnderstanding;
		return request.getRestrictions();
	}
}