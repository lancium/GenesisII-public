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

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.JobIdentification_Type;

public class JobIdentificationRedux extends BaseRedux
{
	private JobIdentification_Type _jobIdentification;
	private String _jobName = null;
	
	public JobIdentificationRedux(IJobPlanProvider provider,
		JobIdentification_Type jobIdentification)
	{
		super(provider);
		
		_jobIdentification = jobIdentification;
	}
	
	public JobIdentification_Type getJobIdentification()
	{
		return _jobIdentification;
	}
	
	public String getJobName()
	{
		return _jobName;
	}
	
	public void consume() throws JSDLException
	{
		if (_jobIdentification != null)
		{
			understandJobIDDescription(_jobIdentification.getDescription());
			understandJobAnnotation(_jobIdentification.getJobAnnotation());
			understandJobName(_jobIdentification.getJobName());
			understandJobProject(_jobIdentification.getJobProject());
			
			MessageElement []any = _jobIdentification.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		}
	}
	
	protected void understandJobIDDescription(String description)
		throws JSDLException
	{
	}
		
	protected void understandJobAnnotation(String []annotations)
		throws JSDLException
	{
	}
	
	protected void understandJobName(String jobName)
		throws JSDLException
	{
		_jobName = jobName;
	}
	
	protected void understandJobProject(String []projects)
		throws JSDLException
	{
	}
}
