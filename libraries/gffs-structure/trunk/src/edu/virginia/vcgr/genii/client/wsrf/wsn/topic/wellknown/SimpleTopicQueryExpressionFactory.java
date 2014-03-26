package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.SimpleTopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpressionFactory;

public class SimpleTopicQueryExpressionFactory implements TopicQueryExpressionFactory
{
	@Override
	public TopicQueryExpression createFromElement(Element e) throws TopicNotSupportedFaultType
	{
		/*
		 * This is SO stupid, but somehow Apache Axis doesn't actually implement the correct
		 * functionality.
		 */
		if (e instanceof MessageElement) {
			try {
				return createFromElement(((MessageElement) e).getAsDOM());
			} catch (TopicNotSupportedFaultType f) {
				throw f;
			} catch (Exception ee) {
				throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());
			}
		}

		String text = e.getTextContent();
		if (text == null)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		text = text.trim();
		int index = text.indexOf(':');
		if (index <= 0)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		String prefix = text.substring(0, index).trim();
		String topic = text.substring(index + 1).trim();
		if (topic.length() == 0)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		String namespaceURI = e.lookupNamespaceURI(prefix);
		if (namespaceURI == null || namespaceURI.length() == 0)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		return new SimpleTopicQueryExpression(new QName(namespaceURI, topic, prefix));
	}
}