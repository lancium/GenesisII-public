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
public class LoopIntegerSweepFunction implements SweepFunction, Serializable
{
	static final long serialVersionUID = 0L;

	static final private int DEFAULT_STEP = 1;

	@XmlTransient
	private int _size = -1;

	@XmlAttribute(name = "start", required = true)
	private int _start;

	@XmlAttribute(name = "end", required = true)
	private int _end;

	@XmlAttribute(name = "step", required = false)
	private Integer _step;

	@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Exception", required = false, nillable = false)
	private Set<Integer> _exceptions;

	public LoopIntegerSweepFunction(int start, int end, int step)
	{
		_start = start;
		_end = end;
		_step = step;

		_exceptions = new HashSet<Integer>();
	}

	public LoopIntegerSweepFunction(int start, int end)
	{
		this(start, end, DEFAULT_STEP);
	}

	public LoopIntegerSweepFunction()
	{
		this(0, 0);
	}

	final public int start()
	{
		return _start;
	}

	final public int end()
	{
		return _end;
	}

	final public int step()
	{
		if (_step != null)
			return _step.intValue();
		else
			return DEFAULT_STEP;
	}

	final public Set<Integer> exceptions()
	{
		return _exceptions;
	}

	@Override
	final public int size()
	{
		if (_size < 0) {
			int count = 0;
			int step = (_step == null) ? 1 : _step.intValue();

			for (int value = _start; value <= _end; value += step) {
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
		private int _nextValue;
		private int _actualStep;
		private Set<Integer> _actualExceptions;

		private LoopIterator()
		{
			_nextValue = _start;
			_actualStep = (_step == null) ? 1 : _step.intValue();
			_actualExceptions = (_exceptions == null) ? new HashSet<Integer>() : _exceptions;

			while (_actualExceptions.contains(_nextValue) && _nextValue <= _end)
				_nextValue += _actualStep;
		}

		@Override
		final public boolean hasNext()
		{
			return _nextValue <= _end;
		}

		@Override
		final public Object next()
		{
			Object ret = new Integer(_nextValue);

			while (true) {
				_nextValue += _actualStep;
				if (_nextValue > _end || !_actualExceptions.contains(new Integer(_nextValue)))
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
