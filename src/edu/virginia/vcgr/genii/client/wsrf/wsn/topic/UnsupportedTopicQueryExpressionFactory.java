package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class UnsupportedTopicQueryExpressionFactory implements
		TopicQueryExpressionFactory
{
	@Override
	public TopicQueryExpression createFromElement(Element e) 
		throws TopicNotSupportedFaultType
	{
		throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());
	}
}