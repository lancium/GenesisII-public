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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTarget;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTargetIdentifier;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
class WholeNodeSweepTarget implements SweepTarget {
	private Node _node;

	static private void removeAllChildren(Node target) {
		NodeList list = target.getChildNodes();
		for (int lcv = 0; lcv < list.getLength(); lcv++)
			target.removeChild(list.item(lcv));
	}

	static private void replaceAllChildren(Node target, Node source) {
		removeAllChildren(target);
		NodeList list = source.getChildNodes();
		for (int lcv = 0; lcv < list.getLength(); lcv++) {
			Node tmp = list.item(lcv);
			tmp = tmp.cloneNode(true);
			target.getOwnerDocument().adoptNode(tmp);
			target.appendChild(tmp);
		}
	}

	public WholeNodeSweepTarget(Node node) {
		_node = node;
	}

	@Override
	public void replace(Object value) throws SweepException {
		if (value instanceof Node) {
			replaceAllChildren(_node, (Node) value);
		} else if (value instanceof String) {
			removeAllChildren(_node);
			_node.setTextContent((String) value);
		} else
			throw new SweepException(String.format(
					"Don't know how to replace a %s with a %s.", Node.class,
					value.getClass()));
	}
}

class WholeNodeSweepTargetIdentifier implements SweepTargetIdentifier {
	private String _expressionString;
	private XPathExpression _expression;

	WholeNodeSweepTargetIdentifier(String expressionString,
			XPathExpression expression) {
		_expressionString = expressionString;
		_expression = expression;
	}

	@Override
	public SweepTarget identify(Node context) throws SweepException {
		try {
			Node evaluationNode = (Node) _expression.evaluate(context,
					XPathConstants.NODE);
			if (evaluationNode == null)
				throw new SweepException(String.format(
						"XPath expression %s didn't match any nodes.",
						_expressionString));

			return new WholeNodeSweepTarget(evaluationNode);
		} catch (XPathExpressionException e) {
			throw new SweepException("Unable to evaluate XPath expression.", e);
		}
	}
}
