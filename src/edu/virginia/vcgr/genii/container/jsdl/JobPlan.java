/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.jsdl;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;


public class JobPlan extends BaseRedux
{
	static protected final String _JSDL_NAMESPACE =
		"http://schemas.ggf.org/jsdl/2005/11/jsdl";
	
	private JobDefinition_Type _jobDefinition;
	
	private JobIdentificationRedux _jobIdentification = null;
	private ApplicationRedux _application = null;
	private ResourcesRedux _resources = null;
	private DataStagingRedux _dataStaging = null;
	
	public JobPlan(IJobPlanProvider provider, JobDefinition_Type jobDefinition)
		throws JSDLException
	{
		super(provider);
		_jobDefinition = jobDefinition;
		
		consume();
	}
	
	public JobDefinition_Type getJobDefinition()
	{
		return _jobDefinition;
	}
	
	public JobIdentificationRedux getJobIdentification()
	{
		return _jobIdentification;
	}
	
	public ApplicationRedux getApplication()
	{
		return _application;
	}
	
	public ResourcesRedux getResources()
	{
		return _resources;
	}
	
	public DataStagingRedux getDataStaging()
	{
		return _dataStaging;
	}
	
	public void verifyComplete() throws JSDLException
	{
		if (_application != null)
			_application.verifyComplete();
		if (_dataStaging != null)
			_dataStaging.verifyComplete();
		if (_jobIdentification != null)
			_jobIdentification.verifyComplete();
		if (_resources != null)
			_resources.verifyComplete();
	}
	
	static public QName toJSDLQName(String elementName)
	{
		return new QName(_JSDL_NAMESPACE, elementName);
	}
	
	protected void understandJobDefinition(JobDefinition_Type jobDefinition)
		throws JSDLException
	{
		if (jobDefinition != null)
		{
			understandJobDescription(jobDefinition.getJobDescription());
			
			MessageElement []any = jobDefinition.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		} else
			throw new InvalidJSDLException(
				"Missing required JobDefinition element.");
	}
	
	protected void understandJobDescription(JobDescription_Type jobDescription)
		throws JSDLException
	{
		if (jobDescription != null)
		{
			_jobIdentification = getProvider().createJobIdentification(
				jobDescription.getJobIdentification());
			_jobIdentification.consume();
			_application = getProvider().createApplication(
				jobDescription.getApplication());
			_application.consume();
			_resources = getProvider().createResources(jobDescription.getResources());
			_resources.consume();
			_dataStaging = getProvider().createDataStaging(
				jobDescription.getDataStaging());
			_dataStaging.consume();
			
			MessageElement []any = jobDescription.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		} else
			throw new InvalidJSDLException(
				"Missing required JobDescription element.");
	}
	
	public void consume() throws JSDLException
	{
		understandJobDefinition(_jobDefinition);
		verifyComplete();
	}
}
