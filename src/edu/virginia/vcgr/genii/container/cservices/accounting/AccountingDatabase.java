package edu.virginia.vcgr.genii.container.cservices.accounting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.pwrapper.ElapsedTime;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.AccountingRecordType;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.security.credentials.identity.Identity;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

class AccountingDatabase
{
	static private Log _logger = LogFactory.getLog(AccountingDatabase.class);
	
	static final private String []CREATE_TABLE_STMTS = {
		"CREATE TABLE accountingrecords(" +
			"arid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"besepi VARCHAR(256)," +
			"arch VARCHAR(64) NOT NULL," +
			"os VARCHAR(64) NOT NULL," +
			"besmachinename VARCHAR(128) NOT NULL," +
			"exitcode INTEGER NOT NULL," +
			"usertimemicrosecs BIGINT NOT NULL," +
			"kerneltimemicrosecs BIGINT NOT NULL," +
			"wallclocktimemicrosecs BIGINT NOT NULL," +
			"maxrssbytes BIGINT NOT NULL," +
			"addtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
		"CREATE TABLE acctcommandlines(" +
			"id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"arid BIGINT NOT NULL," +
			"index INTEGER NOT NULL," +
			"value VARCHAR(512) NOT NULL)",
		"CREATE TABLE credentials(" +
			"cid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"credentialhash INTEGER NOT NULL," +
			"credential BLOB(2G) NOT NULL," +
			"addtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
		"CREATE TABLE acctreccredmap(" +
			"id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"arid BIGINT NOT NULL," +
			"cid BIGINT NOT NULL)",
		"CREATE INDEX acctreccredmaparididx ON acctreccredmap(arid)",
		"CREATE INDEX acctreccredmapcididx ON acctreccredmap(cid)",
		"CREATE INDEX acctcommandlinesarididx ON acctcommandlines(arid)"
	};
	
	static private Calendar convert(Timestamp stamp)
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(stamp.getTime());
		return ret;
	}
	
	static void createTables(Connection connection)
	{
		try
		{
			for (String createStmt : CREATE_TABLE_STMTS)
				DatabaseTableUtils.createTables(
					connection, false, createStmt);
		}
		catch (SQLException sqe)
		{
			_logger.warn("Error trying to create accounting record tables.",
				sqe);
		}
	}
	
	static void addRecord(Connection conn,
		String besepi,
		ProcessorArchitecture arch, OperatingSystemNames os, 
		String machineName, Collection<String> commandLine,
		int exitCode, 
		ElapsedTime user, ElapsedTime kernel, ElapsedTime wallclock, 
		long maximumRSS, Collection<Identity> identities) throws SQLException
	{
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;
		Collection<Long> keys = new Vector<Long>(identities.size());
		
		try
		{
			stmt = conn.prepareStatement(
				"INSERT INTO accountingrecords (besepi," +
					"arch, os, besmachinename, exitcode, usertimemicrosecs, " +
					"kerneltimemicrosecs, wallclocktimemicrosecs, " +
					"maxrssbytes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			if (besepi == null)
				stmt.setNull(1, Types.VARCHAR);
			else
				stmt.setString(1, besepi);
			stmt.setString(2, arch.name());
			stmt.setString(3, os.name());
			stmt.setString(4, machineName);
			stmt.setInt(5, exitCode);
			stmt.setLong(6, user.as(TimeUnit.MICROSECONDS));
			stmt.setLong(7, kernel.as(TimeUnit.MICROSECONDS));
			stmt.setLong(8, wallclock.as(TimeUnit.MICROSECONDS));
			stmt.setLong(9, maximumRSS);
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys();
			if (!rs.next())
				throw new SQLException(
					"Unable to get identity of last added row.");
			long arid = rs.getLong(1);
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			stmt = conn.prepareStatement(
				"INSERT INTO credentials(credentialhash, credential) VALUES (?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			boolean failure = false;
			for (Identity identity : identities)
			{
				stmt.setInt(1, identity.hashCode());
				stmt.setBlob(2, DBSerializer.toBlob(identity,
					"credentials", "credential"));
				stmt.execute();
				rs = stmt.getGeneratedKeys();
				if (!rs.next()) {
					failure = true;
					break;
				}
				keys.add(new Long(rs.getLong(1)));
				rs.close();
				rs = null;
			}
			StreamUtils.close(stmt);
			if (failure) {
				throw new SQLException(
					"Unable to get generated key.");
			}
			
			stmt2 = conn.prepareStatement(
				"INSERT INTO acctreccredmap(arid, cid) VALUES (?, ?)");
			for (Long key : keys)
			{
				stmt2.setLong(1, arid);
				stmt2.setLong(2, key.longValue());
				stmt2.addBatch();
			}
			stmt2.executeBatch();
			
			stmt2.close();
			stmt2 = null;
			
			stmt2 = conn.prepareStatement(
				"INSERT INTO acctcommandlines(index, arid, value) " +
				"VALUES (?, ?, ?)");
			int lcv = 0;
			for (String value : commandLine)
			{
				stmt2.setInt(1, lcv++);
				stmt2.setLong(2, arid);
				stmt2.setString(3, value);
				stmt2.addBatch();
			}
			
			stmt2.executeBatch();
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt2);
			StreamUtils.close(stmt);
		}
	}
	
	/*
	static Collection<AccountingRecordType> getAccountingRecords(
		Connection conn) throws SQLException, IOException
	{
		Collection<AccountingRecordType> ret =
			new LinkedList<AccountingRecordType>();
		
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		
		int numRecords = 0;
		long startTime = System.currentTimeMillis();
		try
		{
			stmt1 = conn.prepareStatement(
				"SELECT * FROM accountingrecords");
			stmt2 = conn.prepareStatement(
				"SELECT c.credential FROM " +
					"acctreccredmap AS a, credentials AS c " +
				"WHERE a.cid = c.cid AND a.arid = ?");
			stmt3 = conn.prepareStatement(
				"SELECT index, value FROM acctcommandlines WHERE arid = ?");
			
			rs1 = stmt1.executeQuery();
			
			while (rs1.next())
			{
				long arid = rs1.getLong("arid");
				Collection<Identity> credentials = new LinkedList<Identity>();
				stmt2.setLong(1, arid);
				rs2 = stmt2.executeQuery();
				while (rs2.next())
				{
					credentials.add(
						(Identity)DBSerializer.fromBlob(rs2.getBlob(1)));
				}
				
				rs2.close();
				rs2 = null;
				
				Vector<String> commandLineValues = new Vector<String>();
				stmt3.setLong(1, arid);
				rs3 = stmt3.executeQuery();
				while (rs3.next())
				{
					int index = rs3.getInt(1);
					String value = rs3.getString(2);
					
					if (index >= commandLineValues.size())
						commandLineValues.setSize(index + 1);
					commandLineValues.set(index, value);
				}
				
				for (int lcv = 0; lcv < commandLineValues.size(); lcv++)
					if (commandLineValues.get(lcv) == null)
						commandLineValues.remove(lcv);
				
				ret.add(new AccountingRecordType(
					arid, rs1.getString("besepi"), rs1.getString("arch"),
					rs1.getString("os"), rs1.getString("besmachinename"),
					commandLineValues.toArray(
						new String[commandLineValues.size()]),
					rs1.getInt("exitcode"), rs1.getLong("usertimemicrosecs"),
					rs1.getLong("kerneltimemicrosecs"),
					rs1.getLong("wallclocktimemicrosecs"),
					rs1.getLong("maxrssbytes"), 
					DBSerializer.serialize(credentials, Long.MAX_VALUE), 
					convert(rs1.getTimestamp("addtime"))));
				numRecords++;
			}
			
			return ret;
		}
		finally
		{
			long stopTime = System.currentTimeMillis();
			
			_logger.info(String.format(
				"Retrieve %d accounting records from db in %d milliseconds.",
				numRecords, (stopTime - startTime)));
			StreamUtils.close(rs3);
			StreamUtils.close(stmt3);
			StreamUtils.close(rs2);
			StreamUtils.close(stmt2);
			StreamUtils.close(rs1);
			StreamUtils.close(stmt1);
		}
	}
	*/
	
//	static Collection<AccountingRecordType> getAccountingRecords(
//		Connection conn) throws SQLException, IOException
//	{
//		Map<Long, AccountingRecord> map =
//			new HashMap<Long, AccountingRecord>();
//		
//		PreparedStatement stmt = null;
//		ResultSet rs = null;
//		
//		long startTime = System.currentTimeMillis();
//		try
//		{
//			stmt = conn.prepareStatement(
//				"SELECT arec.*, c.credential, cl.index, cl.value " +
//				"FROM accountingrecords AS arec, acctreccredmap AS a, credentials AS c, acctcommandlines AS cl " +
//				"WHERE arec.arid = a.arid AND arec.arid = cl.arid AND a.cid = c.cid");
//		
//			rs = stmt.executeQuery();
//			while (rs.next())
//			{
//				long arid = rs.getLong("arid");
//				AccountingRecord ar = map.get(new Long(arid));
//				if (ar == null)
//					map.put(new Long(arid), ar = new AccountingRecord(
//						arid, rs.getString("besepi"), rs.getString("arch"),
//						rs.getString("os"), rs.getString("besmachinename"),
//						rs.getInt("exitcode"), rs.getLong("usertimemicrosecs"),
//						rs.getLong("kerneltimemicrosecs"),
//						rs.getLong("wallclocktimemicrosecs"),
//						rs.getLong("maxrssbytes"),
//						convert(rs.getTimestamp("addtime"))));
//
//				int clineIndex = rs.getInt("index");
//				String clineElement = rs.getString("value");
//				ar.addCommandLineElement(clineIndex, clineElement);
//				
//				Identity id = (Identity)DBSerializer.fromBlob(rs.getBlob("credential"));
//				ar.addCredential(id);
//			}
//			
//			Collection<AccountingRecordType> ret = 
//				new Vector<AccountingRecordType>(map.size());
//			for (AccountingRecord ar : map.values())
//				ret.add(ar.convert());
//			
//			return ret;
//		}
//		finally
//		{
//			long stopTime = System.currentTimeMillis();
//			_logger.info(String.format(
//				"Retrieve %d accounting records from db in %d milliseconds.",
//				map.size(), (stopTime - startTime)));
//			StreamUtils.close(rs);
//			StreamUtils.close(stmt);
//		}
//	}
	
	static Collection<AccountingRecordType> getAccountingRecords(
		Connection conn) throws SQLException, IOException
	{
		Map<Long, AccountingRecord> map =
			new HashMap<Long, AccountingRecord>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		long startTime = System.currentTimeMillis();
		try
		{
			stmt = conn.prepareStatement(
				"SELECT arec.*, cl.index, cl.value " +
				"FROM accountingrecords AS arec, acctcommandlines AS cl " +
				"WHERE arec.arid = cl.arid");
		
			rs = stmt.executeQuery();
			while (rs.next())
			{
				long arid = rs.getLong("arid");
				AccountingRecord ar = map.get(new Long(arid));
				if (ar == null)
					map.put(new Long(arid), ar = new AccountingRecord(
						arid, rs.getString("besepi"), rs.getString("arch"),
						rs.getString("os"), rs.getString("besmachinename"),
						rs.getInt("exitcode"), rs.getLong("usertimemicrosecs"),
						rs.getLong("kerneltimemicrosecs"),
						rs.getLong("wallclocktimemicrosecs"),
						rs.getLong("maxrssbytes"),
						convert(rs.getTimestamp("addtime"))));

				int clineIndex = rs.getInt("index");
				String clineElement = rs.getString("value");
				ar.addCommandLineElement(clineIndex, clineElement);
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			stmt = conn.prepareStatement(
				"SELECT c.credential " +
					"FROM credentials AS c, acctreccredmap AS a " +
				"WHERE a.arid = ? AND a.cid = c.cid");
			
			Collection<AccountingRecordType> ret = 
				new Vector<AccountingRecordType>(map.size());
			for (AccountingRecord ar : map.values())
			{
				stmt.setLong(1, ar.recordID());
				rs = stmt.executeQuery();
				while (rs.next())
				{
					ar.addCredential(
						(Identity)DBSerializer.fromBlob(
							rs.getBlob("credential")));
				}
				
				rs.close();
				rs = null;
				
				ret.add(ar.convert());
			}
			
			return ret;
		}
		finally
		{
			long stopTime = System.currentTimeMillis();
			_logger.info(String.format(
				"Retrieve %d accounting records from db in %d milliseconds.",
				map.size(), (stopTime - startTime)));
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
				
	
	static public void deleteAccountingRecords(
		Connection conn, long lastRecordToDelete) throws SQLException
	{
		PreparedStatement stmt1 = null;

		try
		{
			stmt1 = conn.prepareStatement(
				"DELETE FROM credentials WHERE cid IN " +
					"(SELECT cid FROM acctreccredmap WHERE arid <= ?)");
			stmt1.setLong(1, lastRecordToDelete);
			stmt1.executeUpdate();
			
			stmt1.close();
			stmt1 = null;
			
			stmt1 = conn.prepareStatement(
				"DELETE FROM acctreccredmap WHERE arid <= ?");
			stmt1.setLong(1, lastRecordToDelete);
			stmt1.executeUpdate();
			
			stmt1.close();
			stmt1 = null;
			
			stmt1 = conn.prepareStatement(
				"DELETE FROM accountingrecords WHERE arid <= ?");
			stmt1.setLong(1, lastRecordToDelete);
			stmt1.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt1);
		}
	}
}