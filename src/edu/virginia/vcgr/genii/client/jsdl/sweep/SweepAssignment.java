package edu.virginia.vcgr.genii.client.jsdl.sweep;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.Evaluable;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.EvaluationStep;
import edu.virginia.vcgr.genii.client.jsdl.sweep.eval.SweepTargetIdentifier;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopDoubleSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.LoopIntegerSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.functions.ValuesSweepFunction;
import edu.virginia.vcgr.genii.client.jsdl.sweep.parameters.DocumentNodeSweepParameter;

public class SweepAssignment implements Serializable, Countable, Iterable<Evaluable>
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
	
	/**
	 * For use by XML unmarshalling only.
	 */
	@SuppressWarnings("unused")
	private SweepAssignment()
	{
		this(null);
	}
	
	public SweepAssignment(SweepFunction function, SweepParameter...parameters)
	{
		_function = function;
		_parameters = new Vector<SweepParameter>(parameters.length);
		for (SweepParameter parameter : parameters)
			_parameters.add(parameter);
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

	@Override
	final public Iterator<Evaluable> iterator()
	{
		try
		{
			return new EvaluationIterator();
		}
		catch (SweepException e)
		{
			throw new RuntimeException(
				"Unable to create evaluation iterator.", e);
		}
	}
	
	private class EvaluationIterator implements Iterator<Evaluable>
	{
		private Iterator<Object> _values;
		private List<SweepTargetIdentifier> _targets;
		
		private EvaluationIterator() throws SweepException
		{
			if (_function == null)
				_values = null;
			else
				_values = _function.iterator();
			
			if (_parameters == null)
				_parameters = new Vector<SweepParameter>(0);
			
			_targets = new Vector<SweepTargetIdentifier>(_parameters.size());
			for (SweepParameter parameter : _parameters)
				_targets.add(parameter.targetIdentifier());
		}
		
		@Override
		final public boolean hasNext()
		{
			if (_values == null)
				return false;
			
			return _values.hasNext();
		}

		@Override
		final public Evaluable next()
		{
			return new EvaluationStep(_targets, _values.next());
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove elements from this iterator.");
		}
	}
}