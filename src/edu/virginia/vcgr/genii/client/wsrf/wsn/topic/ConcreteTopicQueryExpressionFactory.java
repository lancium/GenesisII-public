package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.utils.xml.NamespaceResolver;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class ConcreteTopicQueryExpressionFactory implements TopicQueryExpressionFactory
{
	static private class NamespaceResolverImpl implements NamespaceResolver
	{
		private Element _sourceElement;

		private NamespaceResolverImpl(Element sourceElement)
		{
			_sourceElement = sourceElement;
		}

		@Override
		public String getNamespaceURI(String prefix)
		{
			String ret = _sourceElement.lookupNamespaceURI(prefix);
			if (ret == null || ret.isEmpty())
				throw new ConfigurationException(String.format("Unable to lookup prefix %s.", prefix));

			return ret;
		}
	}

	@Override
	public TopicQueryExpression createFromElement(Element e) throws TopicNotSupportedFaultType
	{
		/*
		 * This is SO stupid, but somehow Apache Axis doesn't actually implement the correct
		 * functionallity.
		 */
		if (e instanceof MessageElement) {
			try {
				MessageElement me = (MessageElement) e;
				return createFromElement(me.getAsDOM());
			} catch (TopicNotSupportedFaultType f) {
				throw f;
			} catch (Exception ee) {
				throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());
			}
		}

		NamespaceResolver resolver = new NamespaceResolverImpl(e);
		String expression = null;

		expression = e.getTextContent();

		if (expression == null)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		expression = expression.trim();
		if (expression.length() == 0)
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());

		try {
			TopicPathExpression tpe = TopicPathExpression.fromString(resolver, expression);
			return new ConcreteTopicQueryExpression(tpe.topicPath());
		} catch (ConfigurationException ce) {
			throw FaultManipulator.fillInFault(new TopicNotSupportedFaultType());
		}
	}
}