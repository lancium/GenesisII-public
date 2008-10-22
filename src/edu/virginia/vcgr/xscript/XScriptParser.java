package edu.virginia.vcgr.xscript;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.cmd.tools.xscript.grid.GridParseHandler;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;
import edu.virginia.vcgr.xscript.scriptlang.XScriptParseHandler;

public class XScriptParser
{
	static private Map<String, ParseHandler> _handler;
	
	static
	{
		_handler = new HashMap<String, ParseHandler>();
		
		_handler.put(XScriptConstants.XSCRIPT_NS,
			new XScriptParseHandler());
		_handler.put(GridParseHandler.GRID_NS,
			new GridParseHandler());
	}
	
	static public XScript parse(XScriptEngine engine, InputSource source)
		throws ParserConfigurationException, SAXException, IOException,
			ScriptException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(source);
		
		ParseContext context = new ParseContext()
		{
			@Override
			public ParseHandler findHandler(String namespace)
				throws ScriptException
			{
				ParseHandler handler = _handler.get(namespace);
				if (handler == null)
					throw new ScriptException(String.format(
						"Unable to find parse handler for namespace \"%s\".", 
						namespace));
				return handler;
			}		
		};
		
		Element element = doc.getDocumentElement();
		ParseHandler handler = context.findHandler(element.getNamespaceURI());
		return new XScript(engine, handler.parse(context, element));
	}
	
	static public XScript parse(XScriptEngine engine, Reader reader) 
		throws ParserConfigurationException, SAXException, 
			IOException, ScriptException
	{
		return parse(engine, new InputSource(reader));
	}
	
	static public XScript parse(XScriptEngine engine, File inputFile)
		throws ParserConfigurationException, SAXException, IOException,
			ScriptException
	{
		FileInputStream inputStream = null;
		
		try
		{
			inputStream = new FileInputStream(inputFile);
			return parse(engine, new InputSource(inputStream));
		}
		finally
		{
			if (inputStream != null)
				try { inputStream.close(); } catch (Throwable cause) {}
		}
	}
	
	static public XScript parse(XScriptEngine engine, String script)
		throws ParserConfigurationException, SAXException, IOException,
			ScriptException
	{
		StringReader reader = null;
		
		try
		{
			reader = new StringReader(script);
			return parse(engine, new InputSource(reader));
		}
		finally
		{
			if (reader != null)
				try { reader.close(); } catch (Throwable cause) {}
		}
	}
	
	static public boolean getBoolean(XScriptContext context, String value)
		throws ScriptException
	{
		value = MacroReplacer.replaceMacros(context, value);
		
		if (value.equalsIgnoreCase("true"))
			return true;
		else if (value.equalsIgnoreCase("false"))
			return false;
		
		throw new ScriptException(String.format(
			"A boolean value was expected, but found \"%s\".",
			value));
	}
	
	static public String getAttribute(Element element, String attributeName, 
		String defaultValue)
	{
		NamedNodeMap map = element.getAttributes();
		Node n = map.getNamedItem(attributeName);
		if (n == null)
			return defaultValue;
		String ret = n.getNodeValue();
		if (ret == null)
			ret = defaultValue;
		
		return ret;
	}
	
	static public String getRequiredAttribute(Element element, 
		String attributeName) throws ScriptException
	{
		String ret = getAttribute(element, attributeName, null);
		if (ret == null)
			throw new ScriptException(String.format(
				"Required attribute \"%s\" missing in element <{%s}/%s>.",
				attributeName, element.getNamespaceURI(), 
				element.getLocalName()));
		
		return ret;
	}
	
	static public Element getSingleChild(ParseContext context, Element parent)
		throws ScriptException
	{
		Element child = null;
		NodeList children = parent.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				if (child != null)
					throw new ScriptException(String.format(
						"Only one child element is allowed for a " +
						"<{%s}:%s> node.", parent.getNamespaceURI(),
						parent.getLocalName()));
				child = (Element)n;
			}
		}
		
		return child;
	}
}