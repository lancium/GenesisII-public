package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public interface Evaluable
{
	public void evaluate(EvaluationContext context) throws SweepException;
}