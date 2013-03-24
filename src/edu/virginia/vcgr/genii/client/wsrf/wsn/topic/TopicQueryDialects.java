package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.net.URI;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.namespace.QName;

import org.oasis_open.wsn.base.InvalidFilterFaultType;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.container.util.FaultManipulator;

@XmlEnum(URI.class)
public enum TopicQueryDialects {
	@XmlEnumValue(TopicConstants.SIMPLE_DIALECT_URI)
	Simple(TopicConstants.SIMPLE_DIALECT_URI, new SimpleTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.CONCRETE_DIALECT_URI)
	Concrete(TopicConstants.CONCRETE_DIALECT_URI, new ConcreteTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.FULL_DIALECT_URI)
	Full(TopicConstants.FULL_DIALECT_URI, new UnsupportedTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.XPATH_DIALECT_URI)
	XPath(TopicConstants.XPATH_DIALECT_URI, new UnsupportedTopicQueryExpressionFactory());

	private URI _dialect;
	private TopicQueryExpressionFactory _factory;

	private TopicQueryDialects(String dialect, TopicQueryExpressionFactory factory)
	{
		_dialect = URI.create(dialect);
		_factory = factory;
	}

	final public URI dialect()
	{
		return _dialect;
	}

	static public TopicQueryDialects fromURI(URI uri)
	{
		for (TopicQueryDialects dialect : TopicQueryDialects.values()) {
			if (uri.equals(dialect.dialect()))
				return dialect;
		}

		throw new IllegalArgumentException(String.format("TopicQueryDialect \"%s\" unknown.", uri));
	}

	static public TopicQueryExpression createSimpleExpression(QName root)
	{
		return new SimpleTopicQueryExpression(root);
	}

	static public TopicQueryExpression createConcreteExpression(TopicPath path)
	{
		return new ConcreteTopicQueryExpression(path);
	}

	static public TopicQueryExpression createFromElement(Element e) throws InvalidFilterFaultType, TopicNotSupportedFaultType
	{
		String dialectString = e.getAttribute("Dialect");
		if (dialectString == null)
			throw FaultManipulator.fillInFault(new InvalidFilterFaultType());

		TopicQueryDialects dialect = TopicQueryDialects.fromURI(URI.create(dialectString));

		return dialect._factory.createFromElement(e);
	}
}