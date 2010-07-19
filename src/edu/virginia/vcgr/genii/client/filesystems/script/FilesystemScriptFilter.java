package edu.virginia.vcgr.genii.client.filesystems.script;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemWatchFilter;

public class FilesystemScriptFilter implements FilesystemWatchFilter
{
	static final private String SCRIPT_NS =
		"http://vcgr.cs.virginia.edu/filesystems/filter-script";
	
	static final private QName AND_ELEMENT = new QName(SCRIPT_NS, "and");
	static final private QName OR_ELEMENT = new QName(SCRIPT_NS, "or");
	static final private QName XOR_ELEMENT = new QName(SCRIPT_NS, "xor");
	static final private QName NOT_ELEMENT = new QName(SCRIPT_NS, "not");
	static final private QName TRUE_ELEMENT = new QName(SCRIPT_NS, "true");
	static final private QName FALSE_ELEMENT = new QName(SCRIPT_NS, "false");
	static final private QName LESS_ELEMENT = new QName(SCRIPT_NS, "less");
	static final private QName LESS_EQUALS_ELEMENT = new QName(SCRIPT_NS, "less-equals");
	static final private QName NOT_EQUALS_ELEMENT = new QName(SCRIPT_NS, "not-equals");
	static final private QName EQUALS_ELEMENT = new QName(SCRIPT_NS, "equals");
	static final private QName GREATER_EQUALS_ELEMENT = new QName(SCRIPT_NS, "greater-equals");
	static final private QName GREATER_ELEMENT = new QName(SCRIPT_NS, "greater");
	static final private QName VARIABLE_ELEMENT = new QName(SCRIPT_NS, "variable");
	static final private QName LITERAL_ELEMENT = new QName(SCRIPT_NS, "literal");
	
	static final private String NAME_ATTRIBUTE = "name";
	static final private String VALUE_ATTRIBUTE = "value";
	
	private BooleanExpression _expression;
	
	private FilesystemScriptFilter(BooleanExpression expression)
	{
		_expression = expression;
	}
	
	@Override
	final public boolean matches(FilesystemUsageInformation usageInformation)
	{
		return _expression.evaluate(usageInformation);
	}
	
	@Override
	final public String toString()
	{
		return _expression.toString();
	}
	static private BooleanExpression parseBinaryBoolean(
		BinaryBooleanOperators operator, Element []children)
			throws FilterScriptException
	{
		if (children.length != 2)
			throw new FilterScriptException(String.format(
				"%s binary operator requires exactly 2 child elements.",
				operator));
				
		return BinaryBooleanExpression.createBinaryBooleanExpression(
			parseBooleanExpression(children[0]), operator,
			parseBooleanExpression(children[1]));
	}
	
	static private NumericValueExpression parseNumericValueExpression(
		Element node) throws FilterScriptException
	{
		QName name = XmlUtils.qname(node);
		
		if (name.equals(VARIABLE_ELEMENT))
		{
			String varName = XmlUtils.requiredAttribute(node, NAME_ATTRIBUTE);
			return new VariableNumericValueExpression(varName);
		} else if (name.equals(LITERAL_ELEMENT))
		{
			String value = XmlUtils.requiredAttribute(node, VALUE_ATTRIBUTE);
			return new LiteralNumericValueExpression(value);
		} else
		{
			throw new FilterScriptException(String.format(
				"%s element is not a value element.",
				name));
		}
	}
	
	static private BooleanExpression parseBinaryValueExpression(
		ComparisonOperators operator, Element []children) 
			throws FilterScriptException
	{
		if (children.length != 2)
			throw new FilterScriptException(String.format(
				"%s binary operator requires exactly 2 child elements.",
				operator));
		
		NumericValueExpression left = parseNumericValueExpression(children[0]);
		NumericValueExpression right = parseNumericValueExpression(
			children[1]);
		
		return ComparisonExpression.createComparisonExpression(
			left, operator, right);
	}
	
	static private BooleanExpression parseBooleanExpression(Element node) 
		throws FilterScriptException
	{
		QName name = XmlUtils.qname(node);
		
		if (name.equals(AND_ELEMENT))
		{
			return parseBinaryBoolean(BinaryBooleanOperators.And,
				XmlUtils.getChildren(node));
		} else if (name.equals(OR_ELEMENT))
		{
			return parseBinaryBoolean(BinaryBooleanOperators.Or,
				XmlUtils.getChildren(node));
		} else if (name.equals(XOR_ELEMENT))
		{
			return parseBinaryBoolean(BinaryBooleanOperators.Xor,
				XmlUtils.getChildren(node));
		} else if (name.equals(NOT_ELEMENT))
		{
			Element []children = XmlUtils.getChildren(node);
			if (children.length != 1)
				throw new FilterScriptException(
					"The Not operator requires exactly one child element.");
			return new NotBooleanExpression(parseBooleanExpression(
				children[0]));
		} else if (name.equals(TRUE_ELEMENT))
		{
			return new ConstantBooleanExpression(true);
		} else if (name.equals(FALSE_ELEMENT))
		{
			return new ConstantBooleanExpression(false);
		} else if (name.equals(LESS_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.LessThan,
				XmlUtils.getChildren(node));
		} else if (name.equals(LESS_EQUALS_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.LessThanOrEquals,
				XmlUtils.getChildren(node));
		} else if (name.equals(NOT_EQUALS_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.NotEquals,
				XmlUtils.getChildren(node));
		} else if (name.equals(EQUALS_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.Equals,
				XmlUtils.getChildren(node));
		} else if (name.equals(GREATER_EQUALS_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.GreaterThanOrEquals,
				XmlUtils.getChildren(node));
		} else if (name.equals(GREATER_ELEMENT))
		{
			return parseBinaryValueExpression(ComparisonOperators.GreaterThan,
				XmlUtils.getChildren(node));
		} else
			throw new FilterScriptException(String.format(
				"Unexpectd element \"%s\" encountered.",
				name));
	}
	
	static public FilesystemWatchFilter parseScript(Element filterNode) 
		throws FilterScriptException
	{
		return new FilesystemScriptFilter(parseBooleanExpression(filterNode));
	}
	
	static public FilesystemWatchFilter constantFilter(boolean value)
	{
		return new FilesystemScriptFilter(new ConstantBooleanExpression(value));
	}
	
	static public void main(String []args) throws Throwable
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream in = FilesystemScriptFilter.class.getResourceAsStream("example-script.xml");
		Document doc = builder.parse(in);
		Element e = doc.getDocumentElement();
		FilesystemWatchFilter filter = parseScript(e);
		in.close();
		System.out.println(filter);
	}
}