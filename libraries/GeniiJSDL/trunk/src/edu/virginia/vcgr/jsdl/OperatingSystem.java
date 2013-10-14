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
package edu.virginia.vcgr.jsdl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_osType", "_osVersion", "_description" })
public class OperatingSystem extends CommonJSDLElement
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "OperatingSystemType")
	private OperatingSystemType _osType = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "OperatingSystemVersion")
	private String _osVersion = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Description")
	private String _description;

	public OperatingSystem(OperatingSystemType osType)
	{
		_osType = osType;
	}

	public OperatingSystem()
	{
		this(null);
	}

	final public void osType(OperatingSystemType osType)
	{
		_osType = osType;
	}

	final public OperatingSystemType osType()
	{
		return _osType;
	}

	final public void osVersion(String osVersion)
	{
		_osVersion = osVersion;
	}

	final public String osVersion()
	{
		return _osVersion;
	}

	final public void description(String description)
	{
		_description = description;
	}

	final public String description()
	{
		return _description;
	}
}
