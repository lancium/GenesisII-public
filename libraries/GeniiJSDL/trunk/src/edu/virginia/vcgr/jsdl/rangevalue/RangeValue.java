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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.jsdl.JSDLConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_upperBoundedRange", "_lowerBoundedRange", "_exacts", "_ranges" })
public class RangeValue implements Serializable, Matchable
{
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "UpperBoundedRange", required = false)
	private Boundary _upperBoundedRange;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "LowerBoundedRange", required = false)
	private Boundary _lowerBoundedRange;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Exact", required = false)
	private List<Exact> _exacts;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Range", required = false)
	private List<Range> _ranges;

	public RangeValue()
	{
		_upperBoundedRange = null;
		_lowerBoundedRange = null;
		_exacts = new LinkedList<Exact>();
		_ranges = new LinkedList<Range>();
	}

	final public void upperBoundedRange(Boundary boundary)
	{
		_upperBoundedRange = boundary;
	}

	final public Boundary upperBoundedRange()
	{
		return _upperBoundedRange;
	}

	final public void lowerBoundedRange(Boundary boundary)
	{
		_lowerBoundedRange = boundary;
	}

	final public Boundary lowerBoundedRange()
	{
		return _lowerBoundedRange;
	}

	final public List<Exact> exacts()
	{
		return _exacts;
	}

	final public List<Range> ranges()
	{
		return _ranges;
	}

	@Override
	final public boolean matches(Number number)
	{
		if (_upperBoundedRange != null && _upperBoundedRange.isCeiling(number))
			return true;
		if (_lowerBoundedRange != null && _lowerBoundedRange.isFloor(number))
			return true;

		for (Exact e : _exacts) {
			if (e.matches(number))
				return true;
		}

		for (Range r : _ranges) {
			if (r.matches(number))
				return true;
		}

		return false;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}
}
