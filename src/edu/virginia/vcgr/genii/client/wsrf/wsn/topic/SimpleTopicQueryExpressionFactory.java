package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class SimpleTopicQueryExpressionFactory implements
		TopicQueryExpressionFactory
{
	@Override
	public TopicQueryExpression createFromElement(Element e)
		throws TopicNotSupportedFaultType
	{
		/* This is SO stupid, but somehow Apache Axis doesn't actually
		 * implement the correct functionallity.
		 */
		if (e instanceof MessageElement)
		{
			try
			{
				MessageElement me = (MessageElement)e;
				return createFromElement(me.getAsDOM());
			}
			catch (TopicNotSupportedFaultType f)
			{
				throw f;
			}
			catch (Exception ee)
			{
				throw FaultManipulator.fillInFault(
					new TopicNotSupportedFaultType());
			}
		}
		
		String text = e.getTextContent();
		if (text == null)
			throw FaultManipulator.fillInFault(
				new TopicNotSupportedFaultType());
		
		text = text.trim();
		int index = text.indexOf(':');
		if (index <= 0)
			throw FaultManipulator.fillInFault(
				new TopicNotSupportedFaultType());
		
		String prefix = text.substring(0, index).trim();
		String topic = text.substring(index + 1).trim();
		if (topic.length() == 0)
			throw FaultManipulator.fillInFault(
				new TopicNotSupportedFaultType());
		
		String namespaceURI = e.lookupNamespaceURI(prefix);
		if (namespaceURI == null || namespaceURI.length() == 0)
			throw FaultManipulator.fillInFault(
				new TopicNotSupportedFaultType());
		
		return new SimpleTopicQueryExpression(
			new QName(namespaceURI, topic, prefix));
	}
}