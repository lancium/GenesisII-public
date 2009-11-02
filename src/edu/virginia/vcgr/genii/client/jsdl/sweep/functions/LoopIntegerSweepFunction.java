package edu.virginia.vcgr.genii.client.jsdl.sweep.functions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepFunction;

public class LoopIntegerSweepFunction extends SweepFunction
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
	
	@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Exception",
		required = false, nillable = false)
	private Set<Integer> _exceptions;
	
	public LoopIntegerSweepFunction(int start,
		int end, int step)
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
	
	@Override
	final public int size()
	{
		if (_size < 0)
		{
			int count = 0;
			int step = (_step == null) ? 1 : _step.intValue();
			
			for (int value = _start; value <= _end; value += step)
			{
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
			_actualExceptions = (_exceptions == null) ? 
				new HashSet<Integer>() : _exceptions;
				
			while (_actualExceptions.contains(_nextValue)
				&& _nextValue <= _end)
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
			
			while (true)
			{
				_nextValue += _actualStep;
				if (_nextValue > _end || !_actualExceptions.contains(
					new Integer(_nextValue)))
					break;
			}
			
			return ret;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove elements from this iterator.");
		}
	}
}