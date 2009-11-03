package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import java.util.List;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public class EvaluationStep implements Evaluable
{
	private List<SweepTargetIdentifier> _identifiers;
	private Object _value;
	
	public EvaluationStep(List<SweepTargetIdentifier> identifiers,
		Object value)
	{
		_identifiers = identifiers;
		_value = value;
	}

	@Override
	final public void evaluate(EvaluationContext context) throws SweepException
	{
		for (SweepTargetIdentifier identifier : _identifiers)
			identifier.identify(context.document()).replace(_value);
	}
}