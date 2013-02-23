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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBBESResourceFactory extends BasicDBResourceFactory
{
	static private Log _logger = LogFactory.getLog(DBBESResourceFactory.class);
	
	static private final String []_CREATE_STMTS = new String[] {
		"CREATE TABLE bespolicytable (" +
			"besid VARCHAR(256) NOT NULL PRIMARY KEY," +
			"userloggedinaction VARCHAR(64) NOT NULL," +
			"screensaverinactiveaction VARCHAR(64) NOT NULL)"
	};
	
	public DBBESResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBBESResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
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
	
	@Override
	protected void upgradeTables(Connection conn) throws SQLException
	{
		super.upgradeTables(conn);
		
		PreparedStatement queryStmt = null;
		PreparedStatement insertStmt = null;
		PreparedStatement deleteStmt = null;
		ResultSet rs = null;
		
		try
		{
			queryStmt = conn.prepareStatement(
				"SELECT resourceid, propvalue FROM properties " +
					"WHERE propname = ?");
			insertStmt = conn.prepareStatement(
				"INSERT INTO persistedproperties " +
					"(resourceid, category, propertyname, propertyvalue) " +
				"VALUES(?, ?, ?, ?)");
			deleteStmt = conn.prepareStatement(
				"DELETE FROM properties WHERE resourceid = ? AND propname = ?");
			queryStmt.setString(1, GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY);
			rs = queryStmt.executeQuery();
			
			while (rs.next())
			{
				String resourceid = rs.getString(1);
				Properties props = (Properties)DBSerializer.fromBlob(
					rs.getBlob(2));
				try
				{
					for (Object key : props.keySet())
					{
						insertStmt.setString(1, resourceid);
						insertStmt.setString(2,
							GeniiBESConstants.NATIVE_QUEUE_CONF_CATEGORY);
						insertStmt.setString(3, key.toString());
						insertStmt.setString(4, props.getProperty(key.toString()));
						insertStmt.addBatch();
					}
					
					insertStmt.executeBatch();
					deleteStmt.setString(1, resourceid);
					deleteStmt.setString(2,
						GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY);
					deleteStmt.executeUpdate();
				}
				catch (SQLException sqe)
				{
					_logger.error(String.format(
						"Unable to upgrade nativeq properties for resource %s.",
						resourceid), sqe);
				}
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(queryStmt);
			StreamUtils.close(insertStmt);
		}
	}
}
