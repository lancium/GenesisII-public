package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.xpath;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;

class XPathFunctionalTargetIdentifierFactory
{
	static SweepTargetIdentifier createIdentifier(
		XPath xpath, String functionName, List<String> arguments) throws XPathExpressionException
	{
		if (functionName.equals("substring"))
			return new SubstringFunctionalSweepTargetIdentifier(
				xpath, arguments);
		
		throw new IllegalArgumentException(String.format(
			"Don't know how to evaluate XPath function \"%s\".",
			functionName));
	}
}