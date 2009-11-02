package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationPlan;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationStep;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopDoubleSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopIntegerSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.ValuesSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.DocumentNodeSweepParameter;

public class SweepAssignment implements Serializable, Countable
{
	static final long serialVersionUID = 0L;
	
	@XmlElements({ 
		@XmlElement(namespace = SweepConstants.SWEEP_NS, name = "DocumentNode",
			required = true, nillable = false, 
			type = DocumentNodeSweepParameter.class)
	})
	private List<SweepParameter> _parameters;
	
	@XmlElements({
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Values",
			required = true, nillable = false,
			type = ValuesSweepFunction.class),
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS,
			name = "LoopInteger", required = true, nillable = false,
			type = LoopIntegerSweepFunction.class),
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS,
			name = "LoopDouble", required = true, nillable = false,
			type = LoopDoubleSweepFunction.class)
	})
	private SweepFunction _function;
	
	public SweepAssignment(SweepFunction function, SweepParameter...parameters)
	{
		_function = function;
		_parameters = new Vector<SweepParameter>(parameters.length);
		for (SweepParameter parameter : parameters)
			_parameters.add(parameter);
	}
	
	public SweepAssignment()
	{
		this(null);
	}
	
	final public void addParameter(SweepParameter parameter)
	{
		_parameters.add(parameter);
	}
	
	@Override
	final public int size()
	{
		return _function.size();
	}
	
	final public Iterator<EvaluationPlan> planIterator()
	{
		return new PlanIterator();
	}
	
	private class PlanIterator implements Iterator<EvaluationPlan>
	{
		private Iterator<Object> _functionIterator;
		
		private PlanIterator()
		{
			_functionIterator = _function.iterator();
		}
		
		@Override
		public boolean hasNext()
		{
			return _functionIterator.hasNext();
		}

		@Override
		public EvaluationPlan next()
		{
			Object value = _functionIterator.next();
			EvaluationPlan plan = new EvaluationPlan();
			
			for (SweepParameter parameter : _parameters)
				plan.addStep(new EvaluationStep(
					parameter.targetIdentifier(), value));

			return plan;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove elements from this iterator.");
		}
	}
}