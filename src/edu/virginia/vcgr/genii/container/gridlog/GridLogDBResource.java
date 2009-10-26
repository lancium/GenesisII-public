package edu.virginia.vcgr.genii.container.gridlog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.spi.LoggingEvent;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.gridlog.LoggingEventComparator;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class GridLogDBResource extends BasicDBResource 
	implements GridLogResource
{
	public GridLogDBResource(
		ResourceKey parentKey, 
		DatabaseConnectionPool connectionPool)
			throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	@Override
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = getConnection().prepareStatement(
				"DELETE FROM gridlogevents WHERE loggerid = ?");
			stmt.setString(1, getKey());
			stmt.executeUpdate();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to clean up log events.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void append(String loggerID, LoggingEvent event, String hostname)
			throws ResourceException
	{
		if (loggerID == null)
			loggerID = getKey();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = getConnection().prepareStatement(
				"INSERT INTO gridlogevents(loggerid, contents, hostname) VALUES (?, ?, ?)");
			stmt.setString(1, loggerID);
			stmt.setBlob(2, DBSerializer.toBlob(event, "gridlogevents",
				"contents"));
			stmt.setString(3, hostname);
			
			stmt.executeUpdate();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to append event to grid log.",
				sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	@Override
	public Collection<LogEventInformation> listEvents(String loggerID, boolean sort)
			throws ResourceException
	{
		if (loggerID == null)
			loggerID = getKey();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Vector<LogEventInformation> ret = new Vector<LogEventInformation>();
		
		try
		{
			stmt = getConnection().prepareStatement(
				"SELECT contents, hostname FROM gridlogevents WHERE loggerid = ?");
			stmt.setString(1, loggerID);
			rs = stmt.executeQuery();
			while (rs.next())
				ret.add(new LogEventInformation(rs.getString(2),
					(LoggingEvent)DBSerializer.fromBlob(rs.getBlob(1))));
			
			if (sort)
				Collections.sort(ret, LoggingEventComparator.COMPARATOR);
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Unable to acquire event list from grid log.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}	
}