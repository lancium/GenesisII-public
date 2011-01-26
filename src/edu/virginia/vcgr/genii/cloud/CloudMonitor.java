package edu.virginia.vcgr.genii.cloud;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.cloud.ec2.EC2Manager;
import edu.virginia.vcgr.genii.cloud.ec2.EC2TypicaController;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class CloudMonitor {

	static private DatabaseConnectionPool _connectionPool;
	static private HashMap<String, CloudManager> _cloudManagers =
		new HashMap<String, CloudManager>();
	static private HashMap<String, String> _activities = 
		new HashMap<String, String>();

	public static CloudManager getManager(String besid){
		return _cloudManagers.get(besid);
	}

	public static String getResourceID(String activityID){
		return _activities.get(activityID);
	}


	public static void setConnectionPool(
			DatabaseConnectionPool connectionPool) 
	throws SQLException, IOException{

		if (connectionPool == null)
			throw new IllegalArgumentException(
			"connectionPool argument cannot be null.");

		if (_connectionPool != null)
			throw new IllegalStateException(
			"Cloud connection pool already loaded.");	

		_connectionPool = connectionPool;

	}

	synchronized static public void loadCloudInstance(
			String besid,CloudConfiguration config) throws Exception{

		EC2Manager tManager = new EC2Manager(config, 30, besid);


		EC2TypicaController tController = new EC2TypicaController(
				config.getPublicKey(), config.getSecretKey(),
				config.getEndPoint(), config.getPort(), !config.isEucalyptus(),
				config.isEucalyptus(), config.getKeyPair());

		tController.set_imageID(config.getImageID());
		tManager.setController(tController);

		loadCloudResources(besid, tManager);
		_cloudManagers.put(besid, tManager);

	}

	synchronized static private void loadCloudResources(
			String besid, EC2Manager manager) throws Exception{

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<VMStat> tList = new ArrayList<VMStat>();


		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
					"SELECT resourceid, host, load, port, setup " +
					"FROM cloudResources WHERE besid = ?");
			stmt.setString(1, besid);
			rs = stmt.executeQuery();

			while (rs.next())
			{
				String id = rs.getString(1);
				String host = rs.getString(2);
				int load = rs.getInt(3);
				int port = rs.getInt(4);
				int setup = rs.getInt(5);
				VMStat tStat = new VMStat(id, host, port, load, setup, besid);
				tList.add(tStat);


			}
			connection.commit();
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}

		manager.addVMS(tList);
	}

	synchronized static public void loadCloudActivities(
			) throws SQLException, IOException{
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
			"SELECT activityid, resourceid FROM cloudActivities");
			while (rs.next())
			{
				String activityid = rs.getString(1);
				String resourceid = rs.getString(2);


				_activities.put(activityid, resourceid);


			}
			connection.commit();
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized static public void createResource(
			String besid, String resourceID, String host,
			int port, int load, int setup) throws SQLException
	
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
					"INSERT INTO cloudResources " +
					"(resourceid, host, port, load, besid, setup) " +
			"VALUES (?, ?, ?, ?, ?, ?)");
			stmt.setString(1, resourceID);
			stmt.setString(2, host);
			stmt.setInt(3, port);
			stmt.setInt(4, load);
			stmt.setString(5, besid);
			stmt.setInt(6, setup);


			if (stmt.executeUpdate() != 1)
				throw new SQLException(
				"Unable to update database table for cloud resource creation.");
			connection.commit();

		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}


	synchronized static public void updateResource(
			String besid, String resourceID, int load, int setup)
	throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
					"UPDATE cloudResources SET " +
					"load = ?, setup = ? " +
			"WHERE besid = ? AND resourceID = ?");
			stmt.setInt(1, load);
			stmt.setInt(2, setup);
			stmt.setString(3, besid);
			stmt.setString(4, resourceID);

			if (stmt.executeUpdate() != 1)
				throw new SQLException(
				"Unable to update database table for cloud resource update.");
			connection.commit();

		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}





	synchronized static public void deleteResource(
			String resourceID, String besid) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
			"DELETE FROM cloudResources WHERE besid = ? AND resourceid = ?");
			stmt.setString(1, besid);
			stmt.setString(2, resourceID);

			if (stmt.executeUpdate() != 1)
				throw new SQLException(
				"Unable to update database table for cloudResource deletion.");
			connection.commit();

		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized static public void addActivity(
			String activityID, String resourceID) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
			"INSERT INTO cloudActivities" +
			" (activityid, resourceid) VALUES (?, ?)");
			stmt.setString(1, activityID);
			stmt.setString(2, resourceID);

			if (stmt.executeUpdate() != 1)
				throw new SQLException(
				"Unable to update database table for cloud activity creation.");
			connection.commit();

			_activities.put(activityID, resourceID);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized static public void removeActivity(String activityID)
	throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
			"DELETE FROM cloudActivities WHERE activityid = ?");
			stmt.setString(1, activityID);

			if (stmt.executeUpdate() != 1)
				throw new SQLException(
				"Unable to update database table for cloudResource deletion.");
			connection.commit();
			_activities.remove(activityID);

		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	//Determines if cloud activity, if it is free it
	//Does not fail or block if not cloud Activity
	synchronized static public void freeActivity(String activityID, String besid) throws SQLException{
		if (_activities.containsKey(activityID)){
			CloudManager tManage = getManager(besid);	
			
			if (tManage == null)
				return; //Throw exception, log
				
			tManage.releaseResource(activityID);
		
		}
	}
	
	synchronized static public void deleteCloudBES(String besid) throws Exception{
		//Kill all vms (Activities already killed by BES)
		CloudManager tManage = getManager(besid);
		
		if (tManage == null)
			return; //Throw exception, log
		
		//Free resources associated with BES
		tManage.freeResources();
		
	}


}
