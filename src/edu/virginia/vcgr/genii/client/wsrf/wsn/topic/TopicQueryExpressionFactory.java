package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

public interface TopicQueryExpressionFactory
{
	public TopicQueryExpression createFromElement(Element e) 
		throws TopicNotSupportedFaultType;
}