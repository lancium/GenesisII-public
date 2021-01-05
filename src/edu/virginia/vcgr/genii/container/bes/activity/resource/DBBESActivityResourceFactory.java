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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class DBBESActivityResourceFactory extends BasicDBResourceFactory implements IBESActivityResourceFactory
{
	static private Log _logger = LogFactory.getLog(DBBESActivityResourceFactory.class);

	// 2020-07-13 ASG. Added IPPort to besactivity table. Contains IPADDR:port string
	//LAK: 2020 Aug 27: Added destroyrequested to the DB
	static private final String[] _CREATE_STMTS = new String[] {
		"CREATE TABLE besactivityfaultstable (" + "faultid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
			+ "besactivityid VARCHAR(256) NOT NULL," + "fault BLOB(2G))",
		
		"CREATE TABLE besactivitiestable (" + "activityid VARCHAR(256) NOT NULL PRIMARY KEY," + "besid VARCHAR(256) NOT NULL,"
			+ "jsdl BLOB(2G) NOT NULL," + "owners BLOB(2G) NOT NULL," + "callingcontext BLOB(2G) NOT NULL," + "state BLOB(2G) NOT NULL,"
			+ "submittime TIMESTAMP NOT NULL," + "terminaterequested SMALLINT NOT NULL,"
			+ "activitycwd VARCHAR(256) NOT NULL," + "executionplan BLOB(2G) NOT NULL," + "nextphase INTEGER NOT NULL,"
			+ "activityepr BLOB(2G) NOT NULL," + "activityservicename VARCHAR(128) NOT NULL," + "jobname VARCHAR(256) NOT NULL,"
			+ "suspendrequested SMALLINT NOT NULL," + "destroyrequested SMALLINT NOT NULL," + "ipport VARCHAR(40) NOT NULL," + "persistrequested SMALLINT NOT NULL)",
		
		"CREATE TABLE besactivitypropertiestable (" + "activityid VARCHAR(256) NOT NULL," + "propertyname VARCHAR(256) NOT NULL,"
			+ "propertyvalue BLOB(2G)," + "CONSTRAINT besactivitypropertiesconstraint1 " + "PRIMARY KEY (activityid, propertyname))",
			
		"CREATE INDEX besactivityfaultsindex ON besactivityfaultstable(besactivityid)",
		"CREATE INDEX besactivitiestableindex ON besactivitiestable(besid)" };

	public DBBESActivityResourceFactory(ServerDatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	protected void createTables() throws SQLException
	{
		Connection conn = null;

		// makes no sense to dip down to recreate base tables every time!
		// super.createTables();

		try {
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, _CREATE_STMTS);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}
	
	@Override
	protected void upgradeTables(Connection conn) throws SQLException
	{
		super.upgradeTables(conn);
	
		PreparedStatement alterStmt = null;
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet checkBESPolicyTable_rs = md.getColumns(null, null, "BESACTIVITIESTABLE", "IPPORT");
			if (checkBESPolicyTable_rs.next()) {
				// ipport column exists
				_logger.info("ipport column exists in besactivitestable");
			} else {
				_logger.info("ipport column does not exist in besactivitiestable");
				 // ipport column does not exist
				try {
					alterStmt = conn.prepareStatement("ALTER TABLE " + "besactivitiestable " + "ADD COLUMN " + "ipport " + "VARCHAR(40) NOT NULL " + "DEFAULT 'undefined'" );
					alterStmt.execute();
				} catch (SQLException sqe) {
					_logger.error("Unable to upgrade besactivitiestable with ipport column.", sqe);
				}
			}
		} finally {
			StreamUtils.close(alterStmt);
		}
		
		alterStmt = null;
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet checkBESPolicyTable_rs = md.getColumns(null, null, "BESACTIVITIESTABLE", "DESTROYREQUESTED");
			if (checkBESPolicyTable_rs.next()) {
				// destroyrequested column exists
				_logger.info("destroyrequested column exists in besactivitestable");
			} else {
				_logger.info("destroyrequested column does not exist in besactivitiestable");
				 // destroyrequested column does not exist
				try {
					alterStmt = conn.prepareStatement("ALTER TABLE " + "besactivitiestable " + "ADD COLUMN " + "destroyrequested " + "SMALLINT NOT NULL " + "DEFAULT 0");
					alterStmt.execute();
				} catch (SQLException sqe) {
					_logger.error("Unable to upgrade besactivitiestable with destroyrequested column.", sqe);
				}
			}
		} finally {
			StreamUtils.close(alterStmt);
		}
		
		alterStmt = null;
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet checkBESPolicyTable_rs = md.getColumns(null, null, "BESACTIVITIESTABLE", "PERSISTREQUESTED");
			if (checkBESPolicyTable_rs.next()) {
				// destroyrequested column exists
				_logger.info("persistrequested column exists in besactivitestable");
			} else {
				_logger.info("persistrequested column does not exist in besactivitiestable");
				 // destroyrequested column does not exist
				try {
					alterStmt = conn.prepareStatement("ALTER TABLE " + "besactivitiestable " + "ADD COLUMN " + "persistrequested " + "SMALLINT NOT NULL " + "DEFAULT 0");
					alterStmt.execute();
				} catch (SQLException sqe) {
					_logger.error("Unable to upgrade besactivitiestable with persistrequested column.", sqe);
				}
			}
		} finally {
			StreamUtils.close(alterStmt);
		}
	}

	public IResource instantiate(ResourceKey rKey) throws ResourceException
	{
		try {
			return new DBBESActivityResource(rKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}
