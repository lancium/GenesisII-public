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

import org.ggf.jsdl.FileSystem_Type;

public class FileSystemRedux extends BaseRedux
{
	private FileSystem_Type []_filesystem;
	
	public FileSystemRedux(IJobPlanProvider provider, FileSystem_Type []fileSystem)
	{
		super(provider);
		
		_filesystem = fileSystem;
	}
	
	public FileSystem_Type[] getFileSystems()
	{
		return _filesystem;
	}
	
	public void consume() throws JSDLException
	{
		if (_filesystem != null)
		{
			for (FileSystem_Type filesystem : _filesystem)
			{
				understandIndividualFileSystem(filesystem);
			}
		}
	}
	
	protected void understandIndividualFileSystem(FileSystem_Type filesystem)
		throws JSDLException
	{
		// TODO
		throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("FileSystem"));
	}
}
