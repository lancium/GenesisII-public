package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.xpath;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;

public class XPathTargetIdentifierFactory
{
	static private final Pattern XPATH_FUNCTION_PATTERN = Pattern.compile(
		"^\\s*([^\\(]+)\\(([^\\)]+)\\)\\s*$");
	
	static private List<String> formArgumentList(String argListString)
	{
		if (!argListString.contains(","))
			return new Vector<String>(0);
		
		String []split = argListString.split(",");
		List<String> argList = new Vector<String>(split.length);
		
		for (String item : split)
		{
			item = item.trim();
			if (item.length() != 0)
				argList.add(item);
		}
		
		return argList;
	}
	
	static public SweepTargetIdentifier createIdentifier(
		NamespaceContext nsContext, String xpathExpression) 
			throws SweepException
	{
		try
		{
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			path.setNamespaceContext(nsContext);
			
			Matcher matcher = XPATH_FUNCTION_PATTERN.matcher(xpathExpression);
			if (matcher.matches())
			{
				return XPathFunctionalTargetIdentifierFactory.createIdentifier(
					path, matcher.group(1), formArgumentList(matcher.group(2)));
			} else
				return new WholeNodeSweepTargetIdentifier(
					path.compile(xpathExpression));
		}
		catch (XPathExpressionException e)
		{
			throw new SweepException(
				"Unable to compile XPath expression.", e);
		}
	}
}