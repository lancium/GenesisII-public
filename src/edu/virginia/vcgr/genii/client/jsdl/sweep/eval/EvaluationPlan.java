package edu.virginia.vcgr.genii.client.jsdl.sweep.eval;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepException;

public class EvaluationPlan
{
	private List<EvaluationStep> _steps =
		new LinkedList<EvaluationStep>();
	
	final public void addPlan(EvaluationPlan plan)
	{
		_steps.addAll(plan._steps);
	}
	
	final public void addStep(EvaluationStep step)
	{
		_steps.add(step);
	}
	
	final public void evalutate(Node evalutationContext) throws SweepException
	{
		for (EvaluationStep step : _steps)
			step.evalutate(evalutationContext);
	}
	
	static public EvaluationPlan mergePlans(EvaluationPlan...plans)
	{
		EvaluationPlan ret = new EvaluationPlan();
		
		for (EvaluationPlan plan : plans)
			ret._steps.addAll(plan._steps);
		
		return ret;
	}
	
	static public EvaluationPlan mergePlans(Collection<EvaluationPlan> plans)
	{
		EvaluationPlan ret = new EvaluationPlan();
		
		for (EvaluationPlan plan : plans)
			ret._steps.addAll(plan._steps);
		
		return ret;
	}
}