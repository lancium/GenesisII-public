package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpressionFactory;

public class UnsupportedTopicQueryExpressionFactory implements
		TopicQueryExpressionFactory {
	@Override
	public TopicQueryExpression createFromElement(Element e)
			throws TopicNotSupportedFaultType {
		throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());
	}
}