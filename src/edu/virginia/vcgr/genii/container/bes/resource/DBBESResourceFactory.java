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
package edu.virginia.vcgr.genii.container.bes.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBBESResourceFactory extends BasicDBResourceFactory
{
	static private Log _logger = LogFactory.getLog(QueueDBResourceFactory.class);
	
	static private final String []_CREATE_STMTS = new String[] {
		"CREATE TABLE bespolicytable (" +
			"besid VARCHAR(256) NOT NULL PRIMARY KEY," +
			"userloggedinaction VARCHAR(64) NOT NULL," +
			"screensaverinactiveaction VARCHAR(64) NOT NULL)"
	};
	
	public DBBESResourceFactory(
			DatabaseConnectionPool pool, 
			IResourceKeyTranslater translator)
		throws SQLException
	{
		super(pool, translator);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBBESResource(parentKey, _pool, _translater);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		
		super.createTables();
		
		try
		{
			conn = _pool.acquire();
			stmt = conn.createStatement();
			
			for (String createStmt : _CREATE_STMTS)
			{
				stmt.executeUpdate(createStmt);
			}
			conn.commit();
		}
		catch (SQLException sqe)
		{
			 _logger.debug("Got an exception while creating tables (could be because they exist).", sqe);
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (SQLException sqe) {}
			if (conn != null)
				_pool.release(conn);
		}
	}
}