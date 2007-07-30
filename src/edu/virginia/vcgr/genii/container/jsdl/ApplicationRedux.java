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
import org.ggf.jsdl.Application_Type;


public class ApplicationRedux extends BaseRedux
{
	private Application_Type _application;
	
	public ApplicationRedux(IJobPlanProvider provider, Application_Type application)
	{
		super(provider);
		
		_application = application;
	}
	
	public Application_Type getApplication()
	{
		return _application;
	}
	
	public void consume() throws JSDLException
	{
		if (_application != null)
		{
			understandApplicationName(_application.getApplicationName());
			understandApplicationVersion(_application.getApplicationVersion());
			understandApplicationDescription(_application.getDescription());
			
			MessageElement []any = _application.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		}
	}
	
	protected void understandApplicationName(String applicationName)
		throws JSDLException
	{
		if (applicationName != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("ApplicationName"));
	}
	
	protected void understandApplicationVersion(String applicationVersion)
		throws JSDLException
	{
		if (applicationVersion != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("ApplicationVersion"));
	}
	
	protected void understandApplicationDescription(String applicationDescription)
		throws JSDLException
	{
	}
}
