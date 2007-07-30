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
package edu.virginia.vcgr.genii.container.bes.jsdl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.NCName;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.SourceTarget_Type;

import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.container.jsdl.DataStagingRedux;
import edu.virginia.vcgr.genii.container.jsdl.IJobPlanProvider;
import edu.virginia.vcgr.genii.container.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JobPlan;
import edu.virginia.vcgr.genii.container.jsdl.UnsupportedJSDLElement;

public class SimpleDataStagingRedux extends DataStagingRedux
{
	public SimpleDataStagingRedux(IJobPlanProvider provider, 
		DataStaging_Type []dataStaging)
	{
		super(provider, dataStaging);
	}
	
	protected void understandCreationFlag(CreationFlagEnumeration flag)
		throws JSDLException
	{
	}
	
	protected void understandDeleteOnTermination(Boolean deleteOnTerminate)
		throws JSDLException
	{
		if (deleteOnTerminate != null && !(deleteOnTerminate.booleanValue()))
		{
			QName dotQName = JobPlan.toJSDLQName("DeleteOnTermination");
			throw new UnsupportedJSDLElement("A false value for " +
				dotQName + " is not supported.", dotQName);
		}
	}
	
	protected void understandFileName(String filename)
		throws JSDLException
	{
	}
	
	protected void understandFilesystemName(NCName filesystemName)
		throws JSDLException
	{
		if (filesystemName != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("FileSystemName"));
	}
	
	protected void understandSource(SourceTarget_Type source)
		throws JSDLException
	{
		try
		{
			if (source != null)
			{
				URI uri = new URI(source.getURI().toString());
				if (!URIManager.canRead(uri.getScheme()))
					throw new UnsupportedJSDLElement(
						"Don't know how to read from \"" + uri + "\".",
						JobPlan.toJSDLQName("Source")); 
				
				MessageElement []any = source.get_any();
				if (any != null && any.length > 0)
					throw new UnsupportedJSDLElement(any[0].getQName());
			}
		}
		catch (URISyntaxException use)
		{
			throw new InvalidJSDLException(use.getMessage());
		}
	}
	
	protected void understandTarget(SourceTarget_Type target)
		throws JSDLException
	{
		try
		{
			if (target != null)
			{
				URI uri = new URI(target.getURI().toString());
				if (!URIManager.canWrite(uri.getScheme()))
					throw new UnsupportedJSDLElement(
						"Don't know how to write to \"" + uri + "\".",
						JobPlan.toJSDLQName("Target"));
				
				MessageElement []any = target.get_any();
				if (any != null && any.length > 0)
					throw new UnsupportedJSDLElement(any[0].getQName());
			}
		}
		catch (URISyntaxException use)
		{
			throw new InvalidJSDLException(use.getMessage());
		}
	}
}
