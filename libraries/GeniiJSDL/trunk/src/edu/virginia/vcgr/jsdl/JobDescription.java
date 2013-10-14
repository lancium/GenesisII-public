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
@XmlType(propOrder = { "_jobIdentification", "_application", "_resources", "_staging" })
public class JobDescription extends CommonJSDLElement implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "JobIdentification")
	private JobIdentification _jobIdentification;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Application")
	private Application _application;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Resources")
	private Resources _resources;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "DataStaging")
	private List<DataStaging> _staging = new LinkedList<DataStaging>();

	/**
	 * For use only by XML unmarshalling.
	 */
	@SuppressWarnings("unused")
	private JobDescription()
	{
		this(null, null, null);
	}

	public JobDescription(JobIdentification jobIdentification, Application application, Resources resources)
	{
		_jobIdentification = jobIdentification;
		_application = application;
		_resources = resources;
	}

	final public void jobIdentification(JobIdentification jobIdentification)
	{
		_jobIdentification = jobIdentification;
	}

	final public JobIdentification jobIdentification()
	{
		return _jobIdentification;
	}

	final public void application(Application application)
	{
		_application = application;
	}

	final public Application application()
	{
		return _application;
	}

	final public void resources(Resources resources)
	{
		_resources = resources;
	}

	final public Resources resources()
	{
		return _resources;
	}

	final public List<DataStaging> staging()
	{
		return _staging;
	}
}
