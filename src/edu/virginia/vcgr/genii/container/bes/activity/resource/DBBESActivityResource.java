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
import java.sql.SQLException;

import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
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
		
		BES bes = BES.findBESForActivity(_resourceKey);
		if (bes == null)
			throw new ResourceException(
				"Unable to find bes for activity " + _resourceKey);
		BESActivity activity = bes.findActivity(_resourceKey);
		recursiveDelete(activity.getActivityCWD());
		try
		{
			bes.deleteActivity(_resourceKey);
		}
		catch (UnknownActivityIdentifierFaultType uaift)
		{
			throw new ResourceException("Unable to delete activity.", uaift);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Unable to remove activity from database.", sqe);
		}
	}

	public DBBESActivityResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
	
	public BESActivity findActivity()
		throws ResourceUnknownFaultType
	{
		BES bes = BES.findBESForActivity(_resourceKey);
		if (bes == null)
			throw FaultManipulator.fillInFault(
				new ResourceUnknownFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("Unknown BES \"" +
							_resourceKey + "\".")
				}, null));
		BESActivity activity = bes.findActivity(_resourceKey);
		if (activity == null)
			throw FaultManipulator.fillInFault(
				new ResourceUnknownFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("Unknown BES \"" +
							_resourceKey + "\".")
				}, null));
		
		return activity;
	}
	
	static private void recursiveDelete(File file)
	{
		if (!file.exists())
			return;
		
		if (file.isDirectory())
		{
			for (File subfile : file.listFiles())
				recursiveDelete(subfile);
		}
		
		file.delete();	
	}
}