/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is an implementation of JSDL, JSDL
 * ParameterSweep and other JSDL related specifications from the OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.jsdl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.jsdl.sweep.Sweep;
import edu.virginia.vcgr.jsdl.sweep.SweepConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = JSDLConstants.JSDL_NS, name = "JobDefinition")
public class JobDefinition extends CommonJSDLElement implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlID
	@XmlAttribute(name = "id", required = false)
	private String _id;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Common", required = false)
	private Common _commonBlock;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "JobDescription", required = true)
	private List<JobDescription> _jobDescriptionList = new LinkedList<JobDescription>();

	@XmlElement(namespace = SweepConstants.SWEEP_NS, name = SweepConstants.SWEEP_NAME, required = false)
	private List<Sweep> _parameterSweeps = new LinkedList<Sweep>();

	/**
	 * For use only with XML Unmarshalling.
	 */
	public JobDefinition()
	{
	}

	public JobDefinition(String id)
	{
		_id = id;
	}

	final public List<JobDescription> jobDescription()
	{
		return _jobDescriptionList;
	}

	final public List<Sweep> parameterSweeps()
	{
		return _parameterSweeps;
	}

	final public String id()
	{
		return _id;
	}

	final public void id(String id)
	{
		_id = id;
	}

	final public Common common()
	{
		return _commonBlock;
	}

	final public void common(Common commonBlock)
	{
		_commonBlock = commonBlock;
	}

	@Override
	public String toString()
	{
		// return _commonBlock.toString() + " Number of Job Descriptions: " + _jobDescriptionList.size();
		return "Number of Job Descriptions: " + _jobDescriptionList.size();
	}

}
