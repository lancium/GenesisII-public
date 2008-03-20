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

import java.sql.SQLException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.bes.execution.Activity;
import edu.virginia.vcgr.genii.container.bes.execution.ActivityManager;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBBESActivityResource extends BasicDBResource implements
		IBESActivityResource
{
	@Override
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		ActivityManager mgr = ActivityManager.getManager();
		
		try
		{
			Activity activity = mgr.findActivity(_resourceKey);
			if (activity == null)
				throw new ResourceException("Unable to find activity \"" + 
					_resourceKey + "\".");
			
			ActivityManager.getManager().deleteActivity(activity);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Unable to remove activity from database.", sqe);
		}
	}

	public DBBESActivityResource(
		ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public Activity findActivity() throws ResourceUnknownFaultType
	{
		Activity a = ActivityManager.getManager().findActivity(
			getKey().toString());
		if (a == null)
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription("Unable to find activity.")
				}, null));
		return a;
	}
}