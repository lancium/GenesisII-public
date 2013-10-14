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
package edu.virginia.vcgr.jsdl.sweep;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.jsdl.sweep.eval.Evaluable;
import edu.virginia.vcgr.jsdl.sweep.eval.EvaluationStep;
import edu.virginia.vcgr.jsdl.sweep.eval.SweepTargetIdentifier;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopDoubleSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopIntegerSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.ValuesSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.parameters.DocumentNodeSweepParameter;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class SweepAssignment implements Serializable, Countable, Iterable<Evaluable>
{
	static final long serialVersionUID = 0L;

	@XmlElements({ @XmlElement(namespace = SweepConstants.SWEEP_NS, name = "DocumentNode", required = true, nillable = false,
		type = DocumentNodeSweepParameter.class) })
	private List<SweepParameter> _parameters;

	@XmlElements({
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Values", required = true, nillable = false,
			type = ValuesSweepFunction.class),
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "LoopInteger", required = true, nillable = false,
			type = LoopIntegerSweepFunction.class),
		@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "LoopDouble", required = true, nillable = false,
			type = LoopDoubleSweepFunction.class) })
	private SweepFunction _function;

	/**
	 * For use by XML unmarshalling only.
	 */
	@SuppressWarnings("unused")
	private SweepAssignment()
	{
		this(null);
	}

	public SweepAssignment(SweepFunction function, SweepParameter... parameters)
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

	final public List<SweepParameter> sweepParameters()
	{
		return _parameters;
	}

	final public SweepFunction sweepFunction()
	{
		return _function;
	}

	@Override
	final public int size()
	{
		return _function.size();
	}

	@Override
	final public Iterator<Evaluable> iterator()
	{
		try {
			return new EvaluationIterator();
		} catch (SweepException e) {
			throw new RuntimeException("Unable to create evaluation iterator.", e);
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
			throw new UnsupportedOperationException("Not allowed to remove elements from this iterator.");
		}
	}
}
