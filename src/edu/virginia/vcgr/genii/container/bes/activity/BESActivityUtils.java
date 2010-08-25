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
package edu.virginia.vcgr.genii.container.bes.activity;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.JobDefinition_Type;
import org.oasis_open.wsn.base.Subscribe;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class BESActivityUtils
{
	static private QName JOB_DEF_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "job-definition");
	static private QName CONTAINER_ID_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "container-id");
	
	static public class BESActivityInitInfo
	{
		private String _containerID = null;
		private JobDefinition_Type _jobDef = null;
		private Subscribe _subscribe = null;
	
		public BESActivityInitInfo(JobDefinition_Type jobDef, String containerID,
			Subscribe subscribe)
		{
			_containerID = containerID;
			_jobDef = jobDef;
			_subscribe = subscribe;
		}
		
		public JobDefinition_Type getJobDefinition()
		{
			return _jobDef;
		}
		
		public String getContainerID()
		{
			return _containerID;
		}
		
		public Subscribe getSubscribeRequest()
		{
			return _subscribe;
		}
	}
	
	static public MessageElement[] createCreationProperties(
		JobDefinition_Type jobDefinition, String containerID, 
		BESConstructionParameters nativeqProperties, MessageElement subscribe)
			throws RemoteException
	{
		Collection<MessageElement> ret = new LinkedList<MessageElement>();
		
		ret.add(new MessageElement(JOB_DEF_QNAME, jobDefinition));
		ret.add(new MessageElement(CONTAINER_ID_QNAME, containerID));
		
		if (nativeqProperties != null)
			ret.add(nativeqProperties.serializeToMessageElement());
		if (subscribe != null)
			ret.add(subscribe);
		
		return ret.toArray(new MessageElement[0]);
	}
	
	static public BESActivityInitInfo extractCreationProperties(
		HashMap<QName, Object> properties) throws ResourceException
	{
		JobDefinition_Type jobDef = null;
		String containerID = null;
		Subscribe subscribe = null;
		
		if (properties == null)
			throw new IllegalArgumentException(
				"Can't have a null creation properites parameter.");
		
		if (!properties.containsKey(JOB_DEF_QNAME))
			throw new IllegalArgumentException(
				"Couldn't find job definition document in creation properties.");
		if (!properties.containsKey(CONTAINER_ID_QNAME))
			throw new IllegalArgumentException(
				"Couldn't find container ID in creation properties.");
		
		try
		{
			MessageElement any = (MessageElement)properties.get(JOB_DEF_QNAME);
			jobDef = (JobDefinition_Type)ObjectDeserializer.toObject(
				any, JobDefinition_Type.class);
			containerID = ((MessageElement)properties.get(
				CONTAINER_ID_QNAME)).getValue();
			
			any = (MessageElement)properties.get(
				BESConstants.GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT_QNAME);
			if (any != null)
				subscribe = (Subscribe)ObjectDeserializer.toObject(
					any, Subscribe.class);
		}
		catch (Exception e)
		{
			if (e instanceof ResourceException)
				throw (ResourceException)e;
			
			throw new ResourceException(e.getMessage(), e);
		}
		
		return new BESActivityInitInfo(jobDef, containerID, subscribe);
	}
}
