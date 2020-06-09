/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.container.bes.activity.resource;

import java.io.File;
import java.sql.SQLException;

import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBBESActivityResource extends BasicDBResource implements IBESActivityResource
{
	@Override
	public void destroy() throws ResourceException
	{
		FilesystemManager fsManager = (FilesystemManager) getProperty(FILESYSTEM_MANAGER);
		fsManager.releaseAll();

		String fuseMountDirString = (String) getProperty(FUSE_MOUNT_PROPERTY);
		super.destroy();

		BES bes = BES.findBESForActivity(_resourceKey);
		if (bes == null)
			throw new ResourceException("Unable to find bes for activity " + _resourceKey);
		BESActivity activity = bes.findActivity(_resourceKey);

		BESWorkingDirectory dir = activity.getActivityCWD();

		if (fuseMountDirString != null) {
			File f;
			if (fuseMountDirString.startsWith("/"))
				f = new File(fuseMountDirString);
			else
				f = new File(dir.getWorkingDirectory(), fuseMountDirString);

			File[] entries = f.listFiles();
			if (entries == null || entries.length == 0) {
				if (BESUtilities.isDeletable(dir.getWorkingDirectory()) || dir.mustDelete())
					PersistentDelete.persistentDelete(dir.getWorkingDirectory());
			}
		} else {
			// 2020-06-05 by ASG. This looks like a long-existing bug .. if you terminate a fork/exec job we delete the job dir
			// before the later stages and the clean up .. no accounting, and causes a java equivelant of a seg fault later when
			// trying to read the now non-existent rusage.xml. We may also need to write the exit results file in pwrapper
			/*
			if (BESUtilities.isDeletable(dir.getWorkingDirectory()) || dir.mustDelete())
				PersistentDelete.persistentDelete(dir.getWorkingDirectory());
			 */
		}

		try {
			bes.deleteActivity(getConnection(), _resourceKey);
		} catch (UnknownActivityIdentifierFaultType uaift) {
			throw new ResourceException("Unable to delete activity.", uaift);
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to remove activity from database.", sqe);
		}
		
	}

	public DBBESActivityResource(ResourceKey parentKey, ServerDatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public BESActivity findActivity() throws ResourceUnknownFaultType
	{
		BES bes = BES.findBESForActivity(_resourceKey);
		if (bes == null)
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unknown BES \"" + _resourceKey + "\".") }, null));
		BESActivity activity = bes.findActivity(_resourceKey);
		if (activity == null)
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unknown BES \"" + _resourceKey + "\".") }, null));

		return activity;
	}
}