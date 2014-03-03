package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import java.net.URI;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.namespace.QName;

import org.oasis_open.wsn.base.InvalidFilterFaultType;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.ConcreteTopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.SimpleTopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpressionFactory;

@XmlEnum(URI.class)
public enum TopicQueryDialects implements TopicQueryDialectable {
	@XmlEnumValue(TopicConstants.SIMPLE_DIALECT_URI)
	Simple(TopicConstants.SIMPLE_DIALECT_URI,
			new SimpleTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.CONCRETE_DIALECT_URI)
	Concrete(TopicConstants.CONCRETE_DIALECT_URI,
			new ConcreteTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.FULL_DIALECT_URI)
	Full(TopicConstants.FULL_DIALECT_URI,
			new UnsupportedTopicQueryExpressionFactory()),

	@XmlEnumValue(TopicConstants.XPATH_DIALECT_URI)
	XPath(TopicConstants.XPATH_DIALECT_URI,
			new UnsupportedTopicQueryExpressionFactory());

	private URI _dialect;
	private TopicQueryExpressionFactory _factory;

	TopicQueryDialects(String dialect, TopicQueryExpressionFactory factory) {
		_dialect = URI.create(dialect);
		_factory = factory;
	}

	final public URI dialect() {
		return _dialect;
	}

	final public TopicQueryExpressionFactory getFactory() {
		return _factory;
	}

	static public TopicQueryDialects fromURI(URI uri) {
		for (TopicQueryDialects dialect : TopicQueryDialects.values()) {
			if (uri.equals(dialect.dialect()))
				return dialect;
		}

		throw new IllegalArgumentException(String.format(
				"TopicQueryDialect \"%s\" unknown.", uri));
	}

	static public TopicQueryExpression createSimpleExpression(QName root) {
		return new SimpleTopicQueryExpression(root);
	}

	static public TopicQueryExpression createConcreteExpression(TopicPath path) {
		return new ConcreteTopicQueryExpression(path);
	}

	static public TopicQueryExpression createFromElement(Element e)
			throws InvalidFilterFaultType, TopicNotSupportedFaultType {
		String dialectString = e.getAttribute("Dialect");
		if (dialectString == null)
			throw FaultManipulator.fillInFault(new InvalidFilterFaultType());

		TopicQueryDialects dialect = TopicQueryDialects.fromURI(URI
				.create(dialectString));

		return dialect._factory.createFromElement(e);
	}
}