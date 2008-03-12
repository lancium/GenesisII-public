package edu.virginia.vcgr.genii.client.cmd.tools.xscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.virginia.vcgr.genii.client.cmd.ExceptionHandlerManager;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class XScriptRunner
{
	static private final String GENII_NS =
		"http://vcgr.cs.virginia.edu/genii/xsh/grid";
	static private final String GSH_NS =
		"http://vcgr.cs.virginia.edu/genii/xsh/script";
	
	static private final String SCRIPT_ELEMENT_NAME = "script";
	static private final String FOREACH_ELEMENT_NAME = "foreach";
	static private final String FOR_ELEMENT_NAME = "for";
	static private final String PARAM_ELEMENT_NAME = "param";
	static private final String DEFINE_ELEMENT_NAME = "define";
	static private final String DEFAULT_ELEMENT_NAME = "default";
	static private final String ECHO_ELEMENT_NAME = "echo";
	
	static private final String MESSAGE_ATTRIBUTE_NAME = "message";
	static private final String PARAM_NAME_ATTRIBUTE_NAME = "param-name";
	static private final String SOURCE_DIR_ATTRIBUTE_NAME = "source-dir";
	static private final String SOURCE_FILE_ATTRIBUTE_NAME = "source-file";
	static private final String SOURCE_RNS_ATTRIBUTE_NAME = "source-rns";
	static private final String FILTER_ATTRIBUTE_NAME = "filter";
	static private final String INITIAL_VALUE_ATTRIBUTE_NAME = "initial-value";
	static private final String INCLUSIVE_LIMIT_ATTRIBUTE_NAME = "inclusive-limit";
	static private final String EXCLUSIVE_LIMIT_ATTRIBUTE_NAME = "exclusive-limit";
	static private final String INCREMENT_VALUE_ATTRIBUTE_NAME = "increment-value";
	static private final String NAME_ATTRIBUTE_NAME = "name";
	static private final String SOURCE_ATTRIBUTE_NAME = "source";
	static private final String PATTERN_ATTTRIBUTE_NAME = "pattern";
	static private final String REPLACEMENT_ATTRIBUTE_NAME = "replacement";
	static private final String GLOBAL_ATTRIBUTE_NAME = "global";
	static private final String VALUE_ATTRIBUTE_NAME = "value";
	
	static private QName SCRIPT_ELEMENT = new QName(
		GSH_NS, SCRIPT_ELEMENT_NAME);
	static private QName FOREACH_ELEMENT = new QName(
		GSH_NS, FOREACH_ELEMENT_NAME);
	static private QName FOR_ELEMENT = new QName(
		GSH_NS, FOR_ELEMENT_NAME);
	static private QName PARAM_ELEMENT = new QName(
		GSH_NS, PARAM_ELEMENT_NAME);
	static private QName DEFINE_ELEMENT = new QName(
		GSH_NS, DEFINE_ELEMENT_NAME);
	static private QName DEFAULT_ELEMENT = new QName(
		GSH_NS, DEFAULT_ELEMENT_NAME);
	static private QName ECHO_ELEMENT = new QName(
		GSH_NS, ECHO_ELEMENT_NAME);
	
	static public int runScript(
		File scriptFile,
		IXScriptHandler handler,
		PrintStream out,
		PrintStream err,
		BufferedReader in,
		Properties initialProperties)
	{
		InputStream fin = null;
		
		try
		{
			fin = new FileInputStream(scriptFile);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(fin);
			return parseDocument(doc, handler, out, err, in, initialProperties);
		}
		catch (FileNotFoundException fnfe)
		{
			err.println("The file \"" + 
				scriptFile.getPath() + "\" could not be located.");
			return 1;
		}
		catch (SAXParseException spe)
		{
			err.println("An error occurred on line " +
				spe.getLineNumber() + " of file \"" +
				scriptFile.getPath() + "\".\n\t\"" +
				spe.getLocalizedMessage() + "\".");
			return 1;
		}
		catch (SAXException se)
		{
			se.printStackTrace(err);
			return 1;
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace(err);
			return 1;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace(err);
			return 1;
		}
		catch (Throwable t)
		{
			ExceptionHandlerManager.getExceptionHandler(
				).handleException(t, err);
			return 1;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static private ScopedVariables createInitialVariableScope(Properties initialProperties)
	{
		return new ScopedVariables(initialProperties);
	}
	
	static private QName getQName(Node node)
	{
		return new QName(node.getNamespaceURI(), node.getLocalName());
	}
	
	static public String getAttribute(NamedNodeMap map, String attr, ScopedVariables variables)
	{
		Node n = map.getNamedItem(attr);
		if (n == null)
			return null;
		String val = n.getNodeValue();
		if (val == null)
			return null;
		return replaceMacros(variables, val);
	}
	
	static private int parseDocument(
		Document doc,
		IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in,
		Properties initialProperties)
		throws Throwable
	{
		Element el = doc.getDocumentElement();
		QName nodeName = getQName(el);
		if (!nodeName.equals(SCRIPT_ELEMENT))
		{
			err.println("Root element of script was not \"" +
				SCRIPT_ELEMENT + "\".");
			return 1;
		}
		
		return parseScope(
			createInitialVariableScope(initialProperties), el, handler, out, err, in);
	}
	
	static private int parseScope(
		ScopedVariables variables, Node node, IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable
	{
		NodeList list = node.getChildNodes();
		int numNodes = list.getLength();
		for (int lcv = 0; lcv < numNodes; lcv++)
		{
			Node n = list.item(lcv);
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			int result;
			QName nodeName = getQName(n);
			if (nodeName.getNamespaceURI().equals(GENII_NS))
			{
				result = handleCommand(variables, nodeName.getLocalPart(), n,
					handler, out, err, in);
			} else if (nodeName.equals(FOREACH_ELEMENT))
			{
				result = handleForeach(variables, n,
					handler, out, err, in);
			} else if (nodeName.equals(FOR_ELEMENT))
			{
				result = handleFor(variables, n,
					handler, out, err, in);
			} else if (nodeName.equals(DEFINE_ELEMENT))
			{
				result = handleDefine(variables, n, handler, out, err, in);
			} else if (nodeName.equals(DEFAULT_ELEMENT))
			{
				result = handleDefault(variables, n, handler, out, err, in);
			} else if (nodeName.equals(ECHO_ELEMENT))
			{
				result = handleEcho(variables, n, out, err, in);
			} else
			{
				err.println("Found unexpected node \"" +
					nodeName + "\".  Valid nodes are:");
				err.println("\t" + FOREACH_ELEMENT);
				err.println("\t" + DEFINE_ELEMENT);
				err.println("\t" + DEFAULT_ELEMENT);
				err.println("\tor any Genesis II command node.");
				result = 1;
			}
			
			if (result != 0)
				return result;
		}
		
		return 0;
	}
	
	static private int handleCommand(ScopedVariables variables,
		String commandName, Node n, IXScriptHandler handler, 
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable
	{
		ArrayList<String> cLineStrings = new ArrayList<String>();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			QName childName = getQName(child);
			if (!childName.equals(PARAM_ELEMENT))
			{
				err.println("Only \"" + PARAM_ELEMENT + 
					"\" elements are allowed as children nodes of the \"" + 
					getQName(n) + "\" element.");
				return 1;
			}
			
			cLineStrings.add(replaceMacros(variables, child.getTextContent()));
		}
		
		String []strings = new String[cLineStrings.size()];
		cLineStrings.toArray(strings);
		return handler.handleGridCommand(commandName, 
			strings, out, err, in);
	}
	
	static private int handleForeach(ScopedVariables variables, 
		Node n, IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable
	{
		int numNotNull = 0;
		String error = null;
		
		NamedNodeMap attributes = n.getAttributes();
		String paramName = getAttribute(attributes, PARAM_NAME_ATTRIBUTE_NAME, variables);
		String sourceDir = getAttribute(attributes, SOURCE_DIR_ATTRIBUTE_NAME, variables);
		String sourceFile = getAttribute(attributes, SOURCE_FILE_ATTRIBUTE_NAME, variables);
		String sourceRNS = getAttribute(attributes, SOURCE_RNS_ATTRIBUTE_NAME, variables);
		String filter = getAttribute(attributes, FILTER_ATTRIBUTE_NAME, variables);
		
		if (paramName == null)
			error = "Required attribute \"" + PARAM_NAME_ATTRIBUTE_NAME +
				"\" is missing in " + FOREACH_ELEMENT + " element.";
		if (sourceDir != null)
			numNotNull++;
		if (sourceFile != null)
			numNotNull++;
		if (sourceRNS != null)
			numNotNull++;
		if (numNotNull != 1)
			error = "Exactly one of (" + SOURCE_DIR_ATTRIBUTE_NAME + ", " +
				SOURCE_FILE_ATTRIBUTE_NAME + ", " + SOURCE_RNS_ATTRIBUTE_NAME
				+ ") must be specified as an attribute of the " +
				FOREACH_ELEMENT + " element.";
		
		if (error != null)
		{
			err.println(error);
			return 1;
		}
		
		variables = variables.deriveSubScope();
		ArrayList<String> list = null;
		try
		{
			if (sourceDir != null)
				list = getSourceDirList(replaceMacros(variables, sourceDir), 
					filter);
			else if (sourceFile != null)
				list = getSourceFileList(replaceMacros(variables, sourceFile),
					filter);
			else if (sourceRNS != null)
				list = getSourceRNSList(replaceMacros(variables, sourceRNS),
					filter);
		}
		catch (FileNotFoundException fnfe)
		{
			err.println(fnfe.getLocalizedMessage());
			return 1;
		}
		catch (IOException ioe)
		{
			err.println(ioe.getLocalizedMessage());
			return 1;
		}
		catch (RNSException rne)
		{
			err.println("Error listing contents of RNS directory \"" +
				sourceRNS + "\":  " + rne.getLocalizedMessage());
			return 1;
		}
		catch (Throwable t)
		{
			err.println(t.getLocalizedMessage());
			return 1;
		}
		
		for (String value : list)
		{
			variables.setValue(paramName, value);
			if (parseScope(variables, n, handler, out, err, in) != 0)
				return 1;
		}
		
		return 0;
	}

	static private int handleFor(ScopedVariables variables, 
		Node n, IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in)
		throws Throwable
	{
		long initialValue = 0;
		long inclusiveLimit = 0;
		long exclusiveLimit = 0;
		long incrementValue = 0;
		String error = null;
		
		NamedNodeMap attributes = n.getAttributes();
		String paramName = getAttribute(attributes, PARAM_NAME_ATTRIBUTE_NAME, variables);
		String initialValueString = getAttribute(attributes, INITIAL_VALUE_ATTRIBUTE_NAME, variables);
		String inclusiveLimitString = getAttribute(attributes, INCLUSIVE_LIMIT_ATTRIBUTE_NAME, variables);
		String exclusiveLimitString = getAttribute(attributes, EXCLUSIVE_LIMIT_ATTRIBUTE_NAME, variables);
		String incrementValueString = getAttribute(attributes, INCREMENT_VALUE_ATTRIBUTE_NAME, variables);
		
		if (paramName == null)
			error = "Required attribute \"" + PARAM_NAME_ATTRIBUTE_NAME +
				"\" is missing in " + FOREACH_ELEMENT + " element.";
		
		if (initialValueString == null)
			initialValueString = "0";
		
		try
		{
			initialValue = Long.parseLong(initialValueString);
		}
		catch (NumberFormatException nfe)
		{
			error = "Expected an integer, but saw \"" + initialValueString + "\".";
		}
		
		if (incrementValueString == null)
			incrementValueString = "1";
		
		try
		{
			incrementValue = Long.parseLong(incrementValueString);
		}
		catch (NumberFormatException nfe)
		{
			error = "Expected an integer, but saw \"" + incrementValueString + "\".";
		}
		
		if (inclusiveLimitString != null)
		{
			try
			{
				inclusiveLimit = Long.parseLong(inclusiveLimitString);
				if (exclusiveLimitString != null)
					error = "Cannot specify both an " + INCLUSIVE_LIMIT_ATTRIBUTE_NAME + " attribute and an " +
						EXCLUSIVE_LIMIT_ATTRIBUTE_NAME + " attribute in a for loop.";
			}
			catch (NumberFormatException nfe)
			{
				error = "Expected an integer, but saw \"" + inclusiveLimitString + "\"";
			}
		} else if (exclusiveLimitString != null)
		{
			try
			{
				exclusiveLimit = Long.parseLong(exclusiveLimitString);
			}
			catch (NumberFormatException nfe)
			{
				error = "Expected an integer, but saw \"" + exclusiveLimitString + "\"";
			}
		} else
		{
			error = "One of (" + INCLUSIVE_LIMIT_ATTRIBUTE_NAME + ", " + EXCLUSIVE_LIMIT_ATTRIBUTE_NAME 
				+ ") must be specified on for loops.";
		}
		
		if (error != null)
		{
			err.println(error);
			return 1;
		}
		
		long limit = (inclusiveLimitString != null) ? inclusiveLimit + 1 : exclusiveLimit;
		variables = variables.deriveSubScope();	
		for (long value = initialValue; value < limit; value += incrementValue)
		{
			variables.setValue(paramName, Long.toString(value));
			if (parseScope(variables, n, handler, out, err, in) != 0)
				return 1;
		}
		
		return 0;
	}

	static private int handleDefine(ScopedVariables variables, 
		Node n, IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in)
	{
		String error = null;
		NamedNodeMap attributes = n.getAttributes();
		String varName = getAttribute(attributes, NAME_ATTRIBUTE_NAME, variables);
		String source = getAttribute(attributes, SOURCE_ATTRIBUTE_NAME, variables);
		String pattern = getAttribute(attributes, PATTERN_ATTTRIBUTE_NAME, variables);
		String replacement = getAttribute(attributes, REPLACEMENT_ATTRIBUTE_NAME, variables);
		String global = getAttribute(attributes, GLOBAL_ATTRIBUTE_NAME, variables);
		boolean isGlobal = false;
		
		if (varName == null)
			error = "Define element missing required attribute \"" 
				+ NAME_ATTRIBUTE_NAME + "\".";
		if (source == null)
			error = "Define element missing required attribute \""
				+ SOURCE_ATTRIBUTE_NAME + "\".";
		if (pattern == null)
		{
			if (replacement != null)
				error = "Define element contains \"" + REPLACEMENT_ATTRIBUTE_NAME
					+ "\" attribute without \"" + PATTERN_ATTTRIBUTE_NAME
					+ "\" attribute.";
			if (global != null)
				error = "Define element contains \"" + GLOBAL_ATTRIBUTE_NAME +
					"\" without \"" + PATTERN_ATTTRIBUTE_NAME + "\" attribute.";
		} else
		{
			if (replacement == null)
				error = "Define element contains \"" + PATTERN_ATTTRIBUTE_NAME
				+ "\" without \"" + REPLACEMENT_ATTRIBUTE_NAME + "\" attribute.";
			if (global != null)
				isGlobal = global.equalsIgnoreCase("true");
		}
		
		if (error != null)
		{
			err.println(error);
			return 1;
		}
		
		String value = replaceMacros(variables, source);
		if (pattern != null)
		{
			if (isGlobal)
				value = value.replaceAll(pattern, replacement);
			else
				value = value.replaceFirst(pattern, replacement);
		}
		
		variables.setValue(varName, value);
		return 0;
	}
	
	static private int handleDefault(ScopedVariables variables, 
		Node n, IXScriptHandler handler,
		PrintStream out, PrintStream err, BufferedReader in)
	{
		String error = null;
		NamedNodeMap attributes = n.getAttributes();
		String varName = getAttribute(attributes, NAME_ATTRIBUTE_NAME, variables);
		String value = getAttribute(attributes, VALUE_ATTRIBUTE_NAME, variables);
		
		if (varName == null)
			error = "Default element missing required attribute \"" 
				+ NAME_ATTRIBUTE_NAME + "\".";
		if (value == null)
			error = "Default element missing required attribute \""
				+ VALUE_ATTRIBUTE_NAME + "\".";
		
		if (error != null)
		{
			err.println(error);
			return 1;
		}
		
		if (variables.getValue(varName) == null)
			variables.setValue(varName, replaceMacros(variables, value));
		
		return 0;
	}
	
	static private int handleEcho(ScopedVariables variables, Node n,
		PrintStream out, PrintStream err, BufferedReader in)
	{
		String message = getAttribute(n.getAttributes(), 
			MESSAGE_ATTRIBUTE_NAME, variables);
		if (message == null)
		{
			err.println("Required attribute \"" + MESSAGE_ATTRIBUTE_NAME 
				+ "\" not found in element " + ECHO_ELEMENT + ".");
			return 1;
		}
		
		message = replaceMacros(variables, message);
		out.println(message);
		return 0;
	}
	
	static private String replaceMacros(ScopedVariables variables, String source)
	{
		int lcv;
		int start = -1;
		int end;
		int len = source.length();
		StringBuffer buffer = new StringBuffer(len * 2);
		String macroName;
		
		for (lcv = 0; lcv < len; lcv++)
		{
			char c = source.charAt(lcv);
			if (start < 0)
			{
				if (c == '$')
					start = lcv;
				else
					buffer.append(c);
			} else if (start + 1 == lcv)
			{
				if (c != '{')
				{
					buffer.append('$');
					buffer.append(c);
				}
			} else
			{
				if (c == '}')
				{
					end = lcv;
					macroName = source.substring(start + 2, end);
					buffer.append(variables.getValue(macroName));
					start = -1;
				}
			}
		}
		
		if (start >= 0)
			buffer.append(source.substring(start));
		
		return buffer.toString();
	}
	
	static private ArrayList<String> getSourceDirList(String sourceDir,
		String filter) throws FileNotFoundException
	{
		File source = new File(sourceDir);
		if (!source.exists())
			return new ArrayList<String>();
		
		String []names;
		
		if (filter != null)
			names = source.list(new FilterClass(filter));
		else
			names = source.list();
		
		ArrayList<String> ret = new ArrayList<String>();
		for (String name : names)
		{
			ret.add(name);
		}
		
		return ret;
	}
	
	static private ArrayList<String> getSourceFileList(String sourceFiler,
		String filter) throws FileNotFoundException, IOException
	{
		ArrayList<String> ret = new ArrayList<String>();
		String str;
		Pattern pFilter;
		
		if (filter != null)
			pFilter = Pattern.compile(filter);
		else
			pFilter = Pattern.compile("^.*$");
		
		File source = new File(sourceFiler);
		if (!source.exists())
			return new ArrayList<String>();
		
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(source));
			while ((str = reader.readLine()) != null)
			{
				if (pFilter.matcher(str).matches())
					ret.add(str);
			}
		}
		finally
		{
			StreamUtils.close(reader);
		}
		
		return ret;
	}
	
	static private ArrayList<String> getSourceRNSList(String sourceRNS,
		String filter) throws RNSException, ConfigurationException
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		if (filter == null)
			filter = ".*";
		
		try
		{
			RNSPath []results = RNSPath.getCurrent().list(
					sourceRNS + "/" + filter, RNSPathQueryFlags.MUST_EXIST);
		
			for (RNSPath path : results)
			{
				ret.add(path.getName());
			}
		}
		catch (RNSException rne)
		{
		}
		
		return ret;
	}
	
	static private class FilterClass implements FilenameFilter
	{
		private Pattern _filter;
		
		public FilterClass(String filter)
		{
			_filter = Pattern.compile(filter);
		}
		
		public boolean accept(File dir, String name)
		{
			return _filter.matcher(name).matches();
		}
	}
}
