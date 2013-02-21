package edu.virginia.vcgr.genii.container.q2.resource;

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

public class QueueDBResourceFactory extends BasicDBResourceFactory
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(QueueDBResourceFactory.class);

	static private final String[] _CREATE_STMTS = new String[] {
		"CREATE TABLE q2resources (" + "resourceid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
			+ "resourcename VARCHAR(256) NOT NULL, " + "resourceendpoint BLOB(2G) NOT NULL, "
			+ "queueid VARCHAR(256) NOT NULL, totalslots INTEGER NOT NULL,"
			+ "CONSTRAINT q2resourcesnamecnst UNIQUE (resourcename, queueid))",
		"CREATE TABLE q2jobs (" + "jobid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
			+ "jobticket VARCHAR(256) NOT NULL, " + "queueid VARCHAR(256) NOT NULL, " + "callingcontext BLOB(2G) NOT NULL, "
			+ "jsdl BLOB(2G) NOT NULL, " + "owners BLOB(2G) NOT NULL," + "priority SMALLINT NOT NULL,"
			+ "state VARCHAR(64) NOT NULL, " + "submittime TIMESTAMP NOT NULL, " + "starttime TIMESTAMP, "
			+ "finishtime TIMESTAMP, " + "runattempts SMALLINT NOT NULL, " + "jobendpoint BLOB(2G), " + "resourceid BIGINT, "
			+ "resourceendpoint BLOB(2G), " + "CONSTRAINT q2jobsticket UNIQUE (jobticket, queueid))",
		"CREATE TABLE q2eprs (" + "queueid VARCHAR(256) PRIMARY KEY, " + "queueepr BLOB(2G) NOT NULL)",
		"CREATE TABLE q2errors (" + "errorid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
			+ "queueid VARCHAR(256) NOT NULL, " + "jobid BIGINT NOT NULL, " + "attempt SMALLINT NOT NULL, "
			+ "errors BLOB(2G))",
		"CREATE TABLE q2logs (" + "jobid BIGINT PRIMARY KEY, " + "queueid VARCHAR(256) NOT NULL, "
			+ "logtarget BLOB(2G) NOT NULL, " + "logepr BLOB(2G) NOT NULL)",
		"CREATE TABLE q2joblogtargets(" + "entryid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
			+ "jobid BIGINT NOT NULL, " + "queueid VARCHAR(256) NOT NULL, " + "target BLOB(2G) NOT NULL)",
		"CREATE TABLE q2jobhistorytokens(" + "entryid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
			+ "jobid BIGINT NOT NULL, " + "queueid VARCHAR(256) NOT NULL, " + "historytoken BLOB(2G) NOT NULL)",
		"CREATE INDEX q2joberrorsjobididx ON q2errors(jobid)",
		"CREATE INDEX q2joblogtargetsjobididx ON q2joblogtargets(jobid)",
		"CREATE TABLE q2jobpings (" + "jobid BIGINT PRIMARY KEY," + "failedcommattempts INTEGER NOT NULL)",
		"create table security_headers(jobid BIGINT PRIMARY KEY," + "certificate BLOB(2G))" };

	public QueueDBResourceFactory(DatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new QueueDBResource(parentKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();

		try {
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, _CREATE_STMTS);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}

	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
}
