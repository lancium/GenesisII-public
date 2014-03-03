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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public abstract class FilesystemRelativeValue {
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlAttribute(name = "filesystemName", required = false)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	private String _filesystemName;

	@XmlValue
	private String _value;

	protected FilesystemRelativeValue(String value) {
		set(value);
	}

	protected FilesystemRelativeValue() {
		this(null);
	}

	final public void set(String value) {
		_value = value;
	}

	final public String get() {
		return _value;
	}

	final public void filesystemName(String filesystemName) {
		_filesystemName = filesystemName;
	}

	final public String filesystemName() {
		return _filesystemName;
	}

	final public Map<QName, String> anyAttributes() {
		return _anyAttributes;
	}
}
