package edu.virginia.vcgr.genii.container.scheduler.basic;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;

public class BasicSchedulerDBResource 
	extends RNSDBResource implements IBasicSchedulerResource
{
	public BasicSchedulerDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
}