/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl.sweep.parameters.xpath;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTargetIdentifier;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class XPathTargetIdentifierFactory
{
	static private final Pattern XPATH_FUNCTION_PATTERN = Pattern.compile("^\\s*([^\\(]+)\\(([^\\)]+)\\)\\s*$");

	static private List<String> formArgumentList(String argListString)
	{
		if (!argListString.contains(","))
			return new Vector<String>(0);

		String[] split = argListString.split(",");
		List<String> argList = new Vector<String>(split.length);

		for (String item : split) {
			item = item.trim();
			if (item.length() != 0)
				argList.add(item);
		}

		return argList;
	}

	static public SweepTargetIdentifier createIdentifier(NamespaceContext nsContext, String xpathExpression)
		throws SweepException
	{
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			path.setNamespaceContext(nsContext);

			Matcher matcher = XPATH_FUNCTION_PATTERN.matcher(xpathExpression);
			if (matcher.matches()) {
				return XPathFunctionalTargetIdentifierFactory.createIdentifier(path, matcher.group(1),
					formArgumentList(matcher.group(2)));
			} else
				return new WholeNodeSweepTargetIdentifier(xpathExpression, path.compile(xpathExpression));
		} catch (XPathExpressionException e) {
			throw new SweepException("Unable to compile XPath expression.", e);
		}
	}
}
