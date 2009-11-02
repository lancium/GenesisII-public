package edu.virginia.vcgr.genii.client.jsdl.sweep.functions;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepFunction;

public class ValuesSweepFunction extends SweepFunction
{
	static final long serialVersionUID = 0l;
	
	@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS,
		name = "Value", required = true)
	private List<Object> _values;
	
	public ValuesSweepFunction(Object...element)
	{
		_values = new Vector<Object>(element.length);
		for (Object e : element)
			_values.add(e);
	}
	
	public ValuesSweepFunction()
	{
		_values = new Vector<Object>();
	}
	
	final public void addValue(Object value)
	{
		_values.add(value);
	}
	
	@Override
	final public int size()
	{
		return _values.size();
	}

	@Override
	final public Iterator<Object> iterator()
	{
		return _values.iterator();
	}
}