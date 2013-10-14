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
package edu.virginia.vcgr.jsdl.rangevalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class Boundary implements Serializable
{
	static final long serialVersionUID = 0L;

	static final public boolean DEFAULT_IS_EXCLUSIVE = false;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlAttribute(name = "exclusiveBound", required = false)
	private boolean _isExclusive;

	@XmlValue
	private double _value;

	/**
	 * Default constructor for use with XML Unmarshalling only.
	 */
	@SuppressWarnings("unused")
	private Boundary()
	{
		this(0.0);
	}

	public Boundary(double value, boolean isExclusive)
	{
		_value = value;
		_isExclusive = isExclusive;
	}

	public Boundary(double value)
	{
		this(value, DEFAULT_IS_EXCLUSIVE);
	}

	final public void isExclusive(boolean isExclusive)
	{
		_isExclusive = isExclusive;
	}

	final public boolean isExclusive()
	{
		return _isExclusive;
	}

	final public double value()
	{
		return _value;
	}

	final public void value(double newValue)
	{
		_value = newValue;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}

	final public boolean isCeiling(Number test)
	{
		if (_isExclusive)
			return test.doubleValue() < _value;
		else
			return test.doubleValue() <= _value;
	}

	final public boolean isFloor(Number test)
	{
		if (_isExclusive)
			return test.doubleValue() > _value;
		else
			return test.doubleValue() >= _value;
	}
}
