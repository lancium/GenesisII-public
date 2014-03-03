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
package edu.virginia.vcgr.jsdl.posix;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class Environment extends FilesystemRelativeValue {
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "name", required = true)
	private String _name;

	/**
	 * For use by deserialization only.
	 */
	@SuppressWarnings("unused")
	private Environment() {
	}

	public Environment(String name, String value) {
		super(value);
		name(name);
	}

	public Environment(String name) {
		this(name, null);
	}

	final public void name(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name parameter cannot be null.");

		_name = name;
	}

	final public String name() {
		return _name;
	}
}
