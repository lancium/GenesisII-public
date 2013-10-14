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
package edu.virginia.vcgr.jsdl.sweep.functions;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class LoopDoubleSweepFunction implements SweepFunction, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlTransient
	private int _size = -1;

	@XmlAttribute(name = "start", required = true)
	private double _start;

	@XmlAttribute(name = "end", required = true)
	private double _end;

	@XmlAttribute(name = "step", required = true)
	private double _step;

	@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Exception", required = false, nillable = false)
	private Set<Double> _exceptions;

	public LoopDoubleSweepFunction(double start, double end, double step)
	{
		_start = start;
		_end = end;
		_step = step;

		_exceptions = new HashSet<Double>();
	}

	public LoopDoubleSweepFunction()
	{
		this(0.0, 0.0, 0.1);
	}

	final public double start()
	{
		return _start;
	}

	final public double end()
	{
		return _end;
	}

	final public double step()
	{
		return _step;
	}

	final public Set<Double> exceptions()
	{
		return _exceptions;
	}

	@Override
	final public int size()
	{
		if (_size < 0) {
			int count = 0;

			for (double value = _start; value <= _end; value += _step) {
				if (_exceptions == null || !_exceptions.contains(value))
					count++;
			}

			_size = count;
		}

		return _size;
	}

	@Override
	final public Iterator<Object> iterator()
	{
		return new LoopIterator();
	}

	private class LoopIterator implements Iterator<Object>
	{
		private double _nextValue;
		private Set<Double> _actualExceptions;

		private LoopIterator()
		{
			_nextValue = _start;
			_actualExceptions = (_exceptions == null) ? new HashSet<Double>() : _exceptions;

			while (_actualExceptions.contains(_nextValue) && _nextValue <= _end)
				_nextValue += _step;
		}

		@Override
		final public boolean hasNext()
		{
			return _nextValue <= _end;
		}

		@Override
		final public Object next()
		{
			Object ret = new Double(_nextValue);

			while (true) {
				_nextValue += _step;
				if (_nextValue > _end || !_actualExceptions.contains(new Double(_nextValue)))
					break;
			}

			return ret;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException("Not allowed to remove elements from this iterator.");
		}
	}
}
