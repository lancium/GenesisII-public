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
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_jobName", "_description", "_jobAnnotation",
		"_jobProject" })
public class JobIdentification extends CommonJSDLElement implements
		Serializable {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "JobName")
	private String _jobName;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Description")
	private String _description;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "JobAnnotation")
	private List<String> _jobAnnotation = new LinkedList<String>();

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "JobProject")
	private List<String> _jobProject = new LinkedList<String>();

	public JobIdentification(String jobName) {
		_jobName = jobName;
	}

	public JobIdentification() {
		this(null);
	}

	final public void jobName(String jobName) {
		_jobName = jobName;
	}

	final public String jobName() {
		return _jobName;
	}

	final public void description(String description) {
		_description = description;
	}

	final public String description() {
		return _description;
	}

	final public List<String> annotations() {
		return _jobAnnotation;
	}

	final public List<String> projects() {
		return _jobProject;
	}
}
