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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBBESActivityResourceFactory extends BasicDBResourceFactory
	implements IBESActivityResourceFactory
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(
		DBBESActivityResourceFactory.class);
	
	static private final String []_CREATE_STMTS = new String[] {
		"CREATE TABLE besactivityfaultstable (" +
			"faultid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"besactivityid VARCHAR(256) NOT NULL," +
			"fault BLOB(2G))",
		"CREATE TABLE besactivitiestable (" +
			"activityid VARCHAR(256) NOT NULL PRIMARY KEY," +
			"besid VARCHAR(256) NOT NULL," +
			"jsdl BLOB(2G) NOT NULL," +
			"owners BLOB(2G) NOT NULL," +
			"callingcontext BLOB(2G) NOT NULL," +
			"state BLOB(2G) NOT NULL," +
			"submittime TIMESTAMP NOT NULL," +
			"suspendrequested SMALLINT NOT NULL," +
			"terminaterequested SMALLINT NOT NULL," +
			"activitycwd VARCHAR(256) NOT NULL," +
			"executionplan BLOB(2G) NOT NULL," +
			"nextphase INTEGER NOT NULL," +
			"activityepr BLOB(2G) NOT NULL," +
			"activityservicename VARCHAR(128) NOT NULL," +
			"jobname VARCHAR(256) NOT NULL)",
		"CREATE TABLE besactivitypropertiestable (" +
			"activityid VARCHAR(256) NOT NULL," +
			"propertyname VARCHAR(256) NOT NULL," +
			"propertyvalue BLOB(2G)," +
			"CONSTRAINT besactivitypropertiesconstraint1 " +
			"PRIMARY KEY (activityid, propertyname))",
		"CREATE INDEX besactivityfaultsindex ON besactivityfaultstable(besactivityid)",
		"CREATE INDEX besactivitiestableindex ON besactivitiestable(besid)"
	};
	
	
	public DBBESActivityResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		
		try
		{
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, _CREATE_STMTS);
			conn.commit();
		}
		finally
		{
			_pool.release(conn);
		}
	}
	
	public IResource instantiate(ResourceKey rKey) throws ResourceException
	{
		try
		{
			return new DBBESActivityResource(rKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}
