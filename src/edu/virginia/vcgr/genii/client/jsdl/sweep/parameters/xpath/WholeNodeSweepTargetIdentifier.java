package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.xpath;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTarget;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;

class WholeNodeSweepTarget implements SweepTarget
{
	private Node _node;
	
	static private void removeAllChildren(Node target)
	{
		NodeList list = target.getChildNodes();
		for (int lcv = 0; lcv < list.getLength(); lcv++)
			target.removeChild(list.item(lcv));
	}
	
	static private void replaceAllChildren(Node target, Node source)
	{
		removeAllChildren(target);
		NodeList list = source.getChildNodes();
		for (int lcv = 0; lcv < list.getLength(); lcv++)
		{
			Node tmp = list.item(lcv);
			tmp = tmp.cloneNode(true);
			target.getOwnerDocument().adoptNode(tmp);
			target.appendChild(tmp);
		}
	}
	
	public WholeNodeSweepTarget(Node node)
	{
		_node = node;
	}
	
	@Override
	public void replace(Object value) throws SweepException
	{
		if (value instanceof Node)
		{
			replaceAllChildren(_node, (Node)value);
		} else if (value instanceof String)
		{
			removeAllChildren(_node);
			_node.setTextContent((String)value);
		} else
			throw new SweepException(String.format(
				"Don't know how to replace a %s with a %s.",
				Node.class, value.getClass()));
	}
}

class WholeNodeSweepTargetIdentifier implements SweepTargetIdentifier
{
	private XPathExpression _expression;
	
	WholeNodeSweepTargetIdentifier(XPathExpression expression)
	{
		_expression = expression;
	}
	
	@Override
	public SweepTarget identify(Node context) throws SweepException
	{
		try
		{
			return new WholeNodeSweepTarget((Node)
				_expression.evaluate(context, XPathConstants.NODE));
		}
		catch (XPathExpressionException e)
		{
			throw new SweepException(
				"Unable to evaluate XPath expression.", e);
		}
	}
}