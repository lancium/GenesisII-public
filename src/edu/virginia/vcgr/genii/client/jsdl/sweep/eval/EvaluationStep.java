package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public class EvaluationStep
{
	private SweepTargetIdentifier _identifier;
	private Object _value;
	
	public EvaluationStep(SweepTargetIdentifier identifier, Object value)
	{
		_identifier = identifier;
		_value = value;
	}
	
	final public void evalutate(Node evalutationContext) throws SweepException
	{
		_identifier.identify(evalutationContext).replace(_value);
	}
}