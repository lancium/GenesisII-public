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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import edu.virginia.vcgr.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.jsdl.hpc.HPCProfileApplication;
import edu.virginia.vcgr.jsdl.posix.POSIXApplication;
import edu.virginia.vcgr.jsdl.spmd.SPMDApplication;
import edu.virginia.vcgr.jsdl.spmd.SPMDConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_applicationName", "_applicationVersion",
		"_description", "_application" })
public class Application extends CommonJSDLElement implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "ApplicationName")
	private String _applicationName;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "ApplicationVersion")
	private String _applicationVersion;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Description")
	private String _description;

	@XmlElements({
			@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "POSIXApplication", required = false, nillable = false, type = POSIXApplication.class),
			@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "HPCProfileApplication", required = false, nillable = false, type = HPCProfileApplication.class),
			@XmlElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "SPMDApplication", required = false, nillable = false, type = SPMDApplication.class) })
	private ApplicationBase _application;

	public Application(ApplicationBase application, String name, String version) {
		_application = application;
		_applicationName = name;
		_applicationVersion = version;
	}

	public Application(ApplicationBase application) {
		this(application, null, null);
	}

	public Application() {
		this(null);
	}

	final public void application(ApplicationBase application) {
		_application = application;
	}

	final public ApplicationBase application() {
		return _application;
	}

	final public void applicationName(String name) {
		_applicationName = name;
	}

	final public String applicationName() {
		return _applicationName;
	}

	final public void applicationVersion(String version) {
		_applicationVersion = version;
	}

	final public String applicationVersion() {
		return _applicationVersion;
	}

	final public void description(String description) {
		_description = description;
	}

	final public String description() {
		return _description;
	}
}
