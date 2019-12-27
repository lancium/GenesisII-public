package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.ExclusiveExecutionFacet;

public class DefaultExclusiveExecutionFacet extends DefaultPersonalityFacet implements ExclusiveExecutionFacet
{
	@Override
	public void consumeExclusiveExecution(Object currentUnderstanding, Boolean exclusiveExecution) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "ExclusiveExecution"));
	}
}
