package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.xpath;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTarget;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;

class SubstringNodeSweepTarget implements SweepTarget
{
	private Node _node;
	private int _start;
	private int _length;
	
	public SubstringNodeSweepTarget(Node node, int start, int length)
	{
		_node = node;
		_start = start;
		_length = length;
	}
	
	@Override
	public void replace(Object value) throws SweepException
	{
		String stringValue;
		
		if (value instanceof Node)
			stringValue = ((Node)value).getTextContent();
		else
			stringValue = value.toString();
		
		String original = _node.getTextContent();
		String newValue;
		if (_length >= 0)
			newValue = original.substring(0, _start) +
				stringValue + original.substring(_start + _length);
		else
			newValue = original.substring(0, _start) +
				stringValue;
		
		_node.setTextContent(newValue);
	}
}

class SubstringFunctionalSweepTargetIdentifier
	implements SweepTargetIdentifier
{
	private XPathExpression _nodePath;
	private int _startIndex;
	private int _length;
	
	SubstringFunctionalSweepTargetIdentifier(
		XPath path, List<String> arguments) throws XPathExpressionException
	{
		if (arguments.size() < 2 || arguments.size() > 3)
			throw new IllegalArgumentException("Must have two or three arguments.");
		
		_nodePath = path.compile(arguments.get(0));
		_startIndex = Integer.parseInt(arguments.get(1));
		if (arguments.size() == 3)
			_length = Integer.parseInt(arguments.get(2));
		else
			_length = -1;
	}
	
	@Override
	public SweepTarget identify(Node context) throws SweepException
	{
		try
		{
			return new SubstringNodeSweepTarget(
				(Node)_nodePath.evaluate(context, XPathConstants.NODE),
				_startIndex, _length);
		}
		catch (XPathExpressionException e)
		{
			throw new SweepException(
				"Unable to evaluate XPath expression.", e);
		}
	}
}