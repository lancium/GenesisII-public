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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTarget;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTargetIdentifier;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
class SubstringNodeSweepTarget implements SweepTarget {
	private Node _node;
	private int _start;
	private int _length;

	public SubstringNodeSweepTarget(Node node, int start, int length) {
		_node = node;
		_start = start;
		_length = length;
	}

	@Override
	public void replace(Object value) throws SweepException {
		String stringValue;

		if (value instanceof Node)
			stringValue = ((Node) value).getTextContent();
		else
			stringValue = value.toString();

		String original = _node.getTextContent();
		String newValue;

		DeltaInformation dInfo = (DeltaInformation) _node
				.getUserData(DeltaInformation.USER_HANDLER_KEY);
		if (dInfo == null)
			_node.setUserData(DeltaInformation.USER_HANDLER_KEY,
					dInfo = new DeltaInformation(),
					DeltaInformation.USER_DATA_HANDLER);

		newValue = dInfo.replace(original, stringValue, _start, _length);
		_node.setTextContent(newValue);
	}
}

class SubstringFunctionalSweepTargetIdentifier implements SweepTargetIdentifier {
	private String _stringExpression;
	private XPathExpression _nodePath;
	private int _startIndex;
	private int _length;

	SubstringFunctionalSweepTargetIdentifier(XPath path, List<String> arguments)
			throws XPathExpressionException {
		if (arguments.size() < 2 || arguments.size() > 3)
			throw new IllegalArgumentException(
					"Must have two or three arguments.");

		_stringExpression = arguments.get(0);
		_nodePath = path.compile(_stringExpression);

		/* We subtract 1 because, while XPath is 1 based offsets, I prefer 0 */
		_startIndex = Integer.parseInt(arguments.get(1)) - 1;
		if (arguments.size() == 3)
			_length = Integer.parseInt(arguments.get(2));
		else
			_length = -1;
	}

	@Override
	public SweepTarget identify(Node context) throws SweepException {
		try {
			Node evaluationNode = (Node) _nodePath.evaluate(context,
					XPathConstants.NODE);

			if (evaluationNode == null)
				throw new SweepException(String.format(
						"XPath expression %s didn't match any nodes.",
						_stringExpression));

			return new SubstringNodeSweepTarget(evaluationNode, _startIndex,
					_length);
		} catch (XPathExpressionException e) {
			throw new SweepException("Unable to evaluate XPath expression.", e);
		}
	}
}
