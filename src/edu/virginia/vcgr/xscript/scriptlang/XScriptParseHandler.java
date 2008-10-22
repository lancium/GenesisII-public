package edu.virginia.vcgr.xscript.scriptlang;

import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.xscript.ParseContext;
import edu.virginia.vcgr.xscript.ParseHandler;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.XScriptConstants;
import edu.virginia.vcgr.xscript.XScriptParser;

public class XScriptParseHandler implements ParseHandler
{
	static private ParseStatement parseEcho(
		ParseContext context, Element element) throws ScriptException
	{
		return new EchoStatement(XScriptParser.getRequiredAttribute(
			element, "message"));
	}
	
	static private ParseStatement parseDefine(
		ParseContext context, Element element) throws ScriptException
	{
		Pattern pattern = null;
		String p = XScriptParser.getAttribute(element, "pattern", null);
		String replacement = null;
		if (p != null)
		{
			pattern = Pattern.compile(p);
			replacement = XScriptParser.getRequiredAttribute(
				element, "replacement");
		}
		
		return new DefineStatement(
			XScriptParser.getRequiredAttribute(element, "name"),
			XScriptParser.getRequiredAttribute(element, "source"),
			pattern, replacement, XScriptParser.getAttribute(
				element, "global", "true"));
	}
	
	private ParseStatement parseIf(
		ParseContext context, Element element) throws ScriptException
	{
		String testProperty = XScriptParser.getRequiredAttribute(
			element, "test");
		Element thenBlock = null;
		Element elseBlock = null;
		
		NodeList children = element.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element child = (Element)n;
				String ns = child.getNamespaceURI();
				String name = child.getLocalName();
				
				if (!ns.equals(XScriptConstants.XSCRIPT_NS) ||
					!(name.equals("then") || name.equals("else")))
					throw new ScriptException(String.format(
						"Only <{%s}:%s> and <{%s}:%s> elements are " +
						"allowed as children of a <{%s}:%s> node.", 
						XScriptConstants.XSCRIPT_NS, "then", 
						XScriptConstants.XSCRIPT_NS, "else", 
						XScriptConstants.XSCRIPT_NS, element.getLocalName()));
				
				if (name.equals("then"))
				{
					if (thenBlock != null)
						throw new ScriptException(String.format(
							"Only one <{%s}:%s> is allowed as a child " +
							"of a <{%s}:%s> element.",
							XScriptConstants.XSCRIPT_NS, name,
							XScriptConstants.XSCRIPT_NS, 
							element.getLocalName()));
					thenBlock = child;
				} else
				{
					if (elseBlock != null)
						throw new ScriptException(String.format(
							"Only one <{%s}:%s> is allowed as a child " +
							"of a <{%s}:%s> element.",
							XScriptConstants.XSCRIPT_NS, name,
							XScriptConstants.XSCRIPT_NS, 
							element.getLocalName()));
					elseBlock = child;
				}
			}
		}
		
		if (thenBlock == null)
			throw new ScriptException(String.format(
				"A <{%s}:%s> element must have a <{%s}:then> sub-element.",
				XScriptConstants.XSCRIPT_NS, element.getLocalName()));
		
		ParseStatement thenStmt = parse(context, thenBlock);
		ParseStatement elseStmt = null;
		if (elseBlock != null)
			elseStmt = parse(context, elseBlock);
		
		return new IfStatement(testProperty, thenStmt, elseStmt);
	}
	
	static private ParseStatement parseCondition(
		ParseContext context, Element element) throws ScriptException
	{
		String property = XScriptParser.getRequiredAttribute(
			element, "property");
		
		Element child = XScriptParser.getSingleChild(context, element);
		ParseStatement stmt = context.findHandler(
			child.getNamespaceURI()).parse(context, child);
		if (stmt instanceof ConditionExpression)
		{
			return new ConditionStatement(
				property, (ConditionExpression)stmt);
		} else
			throw new ScriptException(String.format(
				"Children of a <{%s}:%s> element MUST be conditions.",
				element.getNamespaceURI(), element.getLocalName()));
	}
	
	static private ConditionExpression parseEquals(ParseContext context,
		Element element) throws ScriptException
	{
		String arg1 = XScriptParser.getRequiredAttribute(element, "arg1");
		String arg2 = XScriptParser.getRequiredAttribute(element, "arg2");
		String isCaseSensitive = XScriptParser.getAttribute(
			element, "casesensitive", "true");
		
		return new EqualsExpression(arg1, arg2, isCaseSensitive);
	}
	
	static private ConditionExpression parseIsTrueFalse(ParseContext context,
		Element element, boolean isTrue) throws ScriptException
	{
		String value = XScriptParser.getRequiredAttribute(element, "value");
		return new IsTrueFalseExpression(value, isTrue);
	}
	
	static private ConditionExpression parseIsSet(ParseContext context,
		Element element) throws ScriptException
	{
		String property = XScriptParser.getRequiredAttribute(
			element, "property");
		return new IsSetExpression(property);
	}
	
	static private ConditionExpression parseMatches(ParseContext context,
		Element element) throws ScriptException
	{
		String string = XScriptParser.getRequiredAttribute(element, "string");
		String pattern = XScriptParser.getRequiredAttribute(
			element, "pattern");
		
		return new MatchesExpression(string, pattern);
	}
	
	static private ConditionExpression parseCompare(ParseContext context,
		Element element) throws ScriptException
	{
		String isNumeric = XScriptParser.getAttribute(
			element, "numeric", "false");
		String arg1 = XScriptParser.getRequiredAttribute(element, "arg1");
		String arg2 = XScriptParser.getRequiredAttribute(element, "arg2");
		String comparison = XScriptParser.getRequiredAttribute(element, 
			"comparison");
		
		return new CompareExpression(isNumeric, arg1, arg2, comparison);
	}
	
	static private ConditionExpression parseAndOrXor(ParseContext context,
		Element element, String op) throws ScriptException
	{
		MultiAbstractConditionExpression ret;
		
		if (op.equals("and"))
			ret = new AndExpression();
		else if (op.equals("or"))
			ret = new OrExpression();
		else
			ret = new XorExpression();
		
		NodeList children = element.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element child = (Element)n;
				ParseStatement stmt = context.findHandler(
					child.getNamespaceURI()).parse(context, child);
				if (stmt instanceof ConditionExpression)
				{
					ret.addConditionExpression((ConditionExpression)stmt);
				} else
					throw new ScriptException(String.format(
						"Children of a <{%s}:%s> element MUST be conditions.",
						element.getNamespaceURI(), element.getLocalName()));
			}
		}
		
		return ret;
	}
	
	static private ConditionExpression parseNot(ParseContext context,
		Element element) throws ScriptException
	{
		Element child = XScriptParser.getSingleChild(context, element);
		ParseStatement stmt = context.findHandler(
			child.getNamespaceURI()).parse(context, child);
		if (stmt instanceof ConditionExpression)
		{
			return new NotExpression((ConditionExpression)stmt);
		} else
			throw new ScriptException(String.format(
				"Children of a <{%s}:%s> element MUST be conditions.",
				element.getNamespaceURI(), element.getLocalName()));
	}
	
	static private ParseStatement parseExit(ParseContext context,
		Element element) throws ScriptException
	{
		String exitCode = XScriptParser.getRequiredAttribute(
			element, "exitcode");
		return new ExitStatement(exitCode);
	}
	
	static private ParseStatement parseDefault(ParseContext context,
		Element element) throws ScriptException
	{
		String name = XScriptParser.getRequiredAttribute(element, "name");
		String value = XScriptParser.getRequiredAttribute(element, "value");
		return new DefaultStatement(name, value);
	}
	
	static private ParseStatement parseParam(ParseContext context,
		Element element) throws ScriptException
	{
		return new ParamStatement(element.getTextContent());
	}
	
	private ParseStatement parseFor(ParseContext context,
		Element element) throws ScriptException
	{
		String paramName = XScriptParser.getRequiredAttribute(
			element, "param-name");
		String initialValue = XScriptParser.getAttribute(
			element, "initial-value", "0");
		String incrementValue = XScriptParser.getAttribute(
			element, "increment-value", "1");
		String inclusiveLimit = XScriptParser.getAttribute(element, 
			"inclusive-limit", null);
		String exclusiveLimit = XScriptParser.getAttribute(element,
			"exclusive-limit", null);
		
		if (inclusiveLimit == null && exclusiveLimit == null)
			throw new ScriptException(String.format(
				"One of \"inclusive-limit\" or \"exclusive-limit\" is " +
				"required inside of a <{%s}:%s> node.", 
				element.getNamespaceURI(), element.getLocalName()));
		
		ParseStatement innerStatement = parse(context, element);
		
		return new ForStatement(
			paramName, initialValue, inclusiveLimit, exclusiveLimit,
			incrementValue, innerStatement);
	}
	
	private ParseStatement parseForeach(ParseContext context,
		Element element) throws ScriptException
	{
		String paramName = XScriptParser.getRequiredAttribute(
			element, "param-name");
		String sourceDir = XScriptParser.getAttribute(
			element, "source-dir", null);
		String sourceFile = XScriptParser.getAttribute(
			element, "source-file", null);
		String sourceRNS = XScriptParser.getAttribute(
			element, "source-rns", null);
		String filter = XScriptParser.getAttribute(
			element, "filter", null);
		
		int numNonNull = 0;
		if (sourceDir != null)
			numNonNull++;
		if (sourceFile != null)
			numNonNull++;
		if (sourceRNS != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new ScriptException(String.format(
				"Exactly one of \"source-dir\", \"source-file\", or " +
				"\"source-rns\" is required as an attribute of the " +
				"<{%s}:%s> node.", element.getNamespaceURI(), 
				element.getLocalName()));
		
		ParseStatement innerStatement = parse(context, element);
		
		return new ForeachStatement(
			paramName, filter, sourceDir, sourceFile, sourceRNS, 
			innerStatement);
	}
	
	private ParseStatement parseXScriptNode(
		ParseContext context, Element element) throws ScriptException
	{
		String name = element.getLocalName();
		
		if (name.equals("echo"))
			return parseEcho(context, element);
		else if (name.equals("define"))
			return parseDefine(context, element);
		else if (name.equals("condition"))
			return parseCondition(context, element);
		else if (name.equals("if"))
			return parseIf(context, element);
		else if (name.equals("exit"))
			return parseExit(context, element);
		else if (name.equals("default"))
			return parseDefault(context, element);
		else if (name.equals("for"))
			return parseFor(context, element);
		else if (name.equals("foreach"))
			return parseForeach(context, element);
		else
			throw new ScriptException(
				String.format("Don't know how to parse element <{%s}:%s>.",
					XScriptConstants.XSCRIPT_NS, name));
	}
	
	@Override
	public ParseStatement parse(ParseContext context, Element element)
		throws ScriptException
	{
		String name = element.getLocalName();
		
		if (name.equals("equals"))
			return parseEquals(context, element);
		else if (name.equals("istrue"))
			return parseIsTrueFalse(context, element, true);
		else if (name.equals("isfalse"))
			return parseIsTrueFalse(context, element, false);
		else if (name.equals("isset"))
			return parseIsSet(context, element);
		else if (name.equals("matches"))
			return parseMatches(context, element);
		else if (name.equals("compare"))
			return parseCompare(context, element);
		else if (name.equals("and"))
			return parseAndOrXor(context, element, "and");
		else if (name.equals("or"))
			return parseAndOrXor(context, element, "or");
		else if (name.equals("xor"))
			return parseAndOrXor(context, element, "xor");
		else if (name.equals("not"))
			return parseNot(context, element);
		else if (name.equals("param"))
			return parseParam(context, element);
		else
		{
			ScopeStatement ret = new ScopeStatement();
			NodeList children = element.getChildNodes();
			int length = children.getLength();
			for (int lcv = 0; lcv < length; lcv++)
			{
				Node child = children.item(lcv);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					String ns = child.getNamespaceURI();
					if (ns.equals(XScriptConstants.XSCRIPT_NS))
					{
						ret.addStatement(parseXScriptNode(
							context, (Element)child));	
					} else
					{
						ret.addStatement(
							context.findHandler(ns).parse(
								context, (Element)child));
					}
				}
			}
			
			return ret;
		}
	}
}