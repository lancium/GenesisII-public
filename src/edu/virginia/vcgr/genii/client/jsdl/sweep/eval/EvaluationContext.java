package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepListener;

public class EvaluationContext implements Cloneable
{
	private SweepListener _listener;
	private Node _document;
	
	public EvaluationContext(SweepListener listener, Node document)
	{
		_listener = listener;
		_document = document;
	}
	
	@Override
	final public Object clone()
	{
		return new EvaluationContext(
			_listener, _document.cloneNode(true));
	}
	
	final public void emit() throws SweepException
	{
		_listener.emitSweepInstance(_document);
	}
	
	final public Node document()
	{
		return _document;
	}
}