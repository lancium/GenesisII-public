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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.jsdl.JSDLConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_lowerBound", "_upperBound" })
public class Range implements Serializable, Matchable
{
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "LowerBound", required = true)
	private Boundary _lowerBound;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "UpperBound", required = true)
	private Boundary _upperBound;

	/**
	 * This constructor is for XML unmarshalling only.
	 */
	@SuppressWarnings("unused")
	private Range()
	{
		_lowerBound = null;
		_upperBound = null;
	}

	public Range(Boundary lowerBound, Boundary upperBound)
	{
		if (lowerBound == null)
			throw new IllegalArgumentException("lowerBound argument cannot be null.");

		if (upperBound == null)
			throw new IllegalArgumentException("upperBound argument cannot be null.");

		_lowerBound = lowerBound;
		_upperBound = upperBound;
	}

	final public void lowerBound(Boundary lowerBound)
	{
		_lowerBound = lowerBound;
	}

	final public Boundary lowerBound()
	{
		return _lowerBound;
	}

	final public void upperBound(Boundary upperBound)
	{
		_upperBound = upperBound;
	}

	final public Boundary upperBound()
	{
		return _upperBound;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}

	@Override
	final public boolean matches(Number number)
	{
		return _lowerBound.isFloor(number) && _upperBound.isCeiling(number);
	}
}
