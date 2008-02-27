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

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityStatusType;

import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class BESActivityAttributeHandler extends AbstractAttributeHandler
{	
	public BESActivityAttributeHandler(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(BESActivityConstants.STATUS_ATTR, "getStatusAttr");
	}
	
	private ActivityStatusType getStatus() 
		throws ResourceException, ResourceUnknownFaultType
	{
		IBESActivityResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IBESActivityResource)rKey.dereference();
		return resource.getOverallStatus();
	}
	
	public MessageElement getStatusAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(
			BESActivityConstants.STATUS_ATTR, getStatus());
	}
}
