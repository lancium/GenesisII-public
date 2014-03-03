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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class ValuesSweepFunction implements SweepFunction, Serializable {
	static final long serialVersionUID = 0l;

	@XmlElement(namespace = SweepConstants.SWEEP_FUNC_NS, name = "Value", required = true)
	private List<Object> _values;

	public ValuesSweepFunction(Object... element) {
		_values = new Vector<Object>(element.length);
		for (Object e : element)
			_values.add(e);
	}

	public ValuesSweepFunction() {
		_values = new Vector<Object>();
	}

	final public void addValue(Object value) {
		_values.add(value);
	}

	final public List<Object> values() {
		return _values;
	}

	@Override
	final public int size() {
		return _values.size();
	}

	@Override
	final public Iterator<Object> iterator() {
		return _values.iterator();
	}
}
