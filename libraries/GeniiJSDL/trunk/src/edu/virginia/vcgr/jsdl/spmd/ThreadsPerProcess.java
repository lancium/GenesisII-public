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
package edu.virginia.vcgr.jsdl.spmd;

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
public class ThreadsPerProcess implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlAttribute(name = "actualindividualcpucount", required = false)
	private Boolean _actualIndividualCPUCount = null;

	@XmlValue
	private Long _value = null;

	private ThreadsPerProcess() {
		this(null, null);
	}

	private ThreadsPerProcess(Long value, Boolean actualIndividualCPUCount) {
		_actualIndividualCPUCount = actualIndividualCPUCount;
		_value = value;
	}

	final public boolean actualIndividualCPUCount() {
		if (_actualIndividualCPUCount == null)
			return false;

		return _actualIndividualCPUCount;
	}

	final public Long value() {
		return _value;
	}

	final public Map<QName, String> anyAttributes() {
		return _anyAttributes;
	}

	static public ThreadsPerProcess threadsPerProcess(long value) {
		return new ThreadsPerProcess(value, null);
	}

	static public ThreadsPerProcess useActualIndividualCPUCount() {
		return new ThreadsPerProcess(null, true);
	}
}
