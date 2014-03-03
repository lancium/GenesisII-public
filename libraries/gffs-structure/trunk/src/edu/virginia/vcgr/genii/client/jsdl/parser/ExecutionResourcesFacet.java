package edu.virginia.vcgr.genii.client.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultResourcesFacet;

public class ExecutionResourcesFacet extends DefaultResourcesFacet {
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
			throws JSDLException {
		JobRequest request = (JobRequest) parentUnderstanding;
		return request.getRestrictions();
	}
}