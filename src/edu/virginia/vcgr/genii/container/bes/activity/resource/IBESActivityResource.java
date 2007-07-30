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
package edu.virginia.vcgr.genii.container.bes.activity.resource;

import java.io.File;

import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IBESActivityResource extends IResource
{
	static public final String ACTIVITY_NAME_PROPERTY = "activity-name";
	
	public void createProcess(EndpointReferenceType activityEPR,
		File basedir, JobDefinition_Type jobDef)
			throws ResourceException, JSDLException;
	public void restartProcessing() throws ResourceException, JSDLException;
	
	public JobDefinition_Type getJobDefinition() throws ResourceException;
	
	public void associateWithContainer(EndpointReferenceType activityEPR,
		String containerID) throws ResourceException;
	
	public ActivityStatusType getOverallStatus() throws ResourceException;
	public boolean terminateActivity() 
		throws ResourceException, ResourceUnknownFaultType;
}
