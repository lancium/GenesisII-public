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

import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.FileSystem_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.jsdl.Resources_Type;


public interface IJobPlanProvider
{
	public JobIdentificationRedux createJobIdentification(
		JobIdentification_Type jobIdentification);
	public ApplicationRedux createApplication(
		Application_Type application);
	public ResourcesRedux createResources(
		Resources_Type resources);
	public DataStagingRedux createDataStaging(
		DataStaging_Type []dataStaging);
	
	public FileSystemRedux createFileSystem(FileSystem_Type []filesystem);
}
