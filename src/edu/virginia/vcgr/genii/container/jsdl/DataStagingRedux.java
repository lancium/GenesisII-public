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

import org.apache.axis.types.NCName;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.SourceTarget_Type;

public class DataStagingRedux extends BaseRedux
{
	private DataStaging_Type []_dataStaging;
	
	public DataStagingRedux(IJobPlanProvider provider, 
		DataStaging_Type []dataStaging)
	{
		super(provider);
		
		_dataStaging = dataStaging;
	}
	
	public DataStaging_Type[] getDataStaging()
	{
		return _dataStaging;
	}
	
	public void consume() throws JSDLException
	{
		if (_dataStaging != null)
		{
			for (DataStaging_Type dataItem : _dataStaging)
			{
				understandIndividualDataStaging(dataItem);
			}
		}
	}
	
	protected void understandIndividualDataStaging(DataStaging_Type dataItem)
		throws JSDLException
	{
		if (dataItem != null)
		{
			understandCreationFlag(dataItem.getCreationFlag());
			understandDeleteOnTermination(dataItem.getDeleteOnTermination());
			understandFileName(dataItem.getFileName());
			understandFilesystemName(dataItem.getFilesystemName());
			understandSource(dataItem.getSource());
			understandTarget(dataItem.getTarget());
		}
	}
	
	protected void understandCreationFlag(CreationFlagEnumeration flag)
		throws JSDLException
	{
		if (flag != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("CreationFlag"));
	}
	
	protected void understandDeleteOnTermination(Boolean deleteOnTerminate)
		throws JSDLException
	{
		if (deleteOnTerminate != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("DeleteOnTermination"));	
	}
	
	protected void understandFileName(String filename)
		throws JSDLException
	{
		if (filename != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("FileName"));
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
		if (source != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("Source"));
	}
	
	protected void understandTarget(SourceTarget_Type target)
		throws JSDLException
	{
		if (target != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("Target"));
	}
}
