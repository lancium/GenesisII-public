package edu.virginia.vcgr.genii.container.bes;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.postlog.JobEvent;
import edu.virginia.vcgr.genii.client.postlog.PostTargets;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class BES implements Closeable
{
	static private Log _logger = LogFactory.getLog(BES.class);
	
	static private DatabaseConnectionPool _connectionPool;
	
	static private HashMap<String, BES> _knownInstances =
		new HashMap<String, BES>();
	static private HashMap<String, BES> _activityToBESMap =
		new HashMap<String, BES>();
	
	static private void addActivityToBESMapping(
		String activityid, BES bes)
	{
		synchronized(_activityToBESMap)
		{
			_activityToBESMap.put(activityid, bes);
		}
	}
	
	static private void removeActivityToBESMapping(String activityid)
	{
		synchronized(_activityToBESMap)
		{
			_activityToBESMap.remove(activityid);
		}
	}
	
	static public BES findBESForActivity(String activityid)
	{
		synchronized(_activityToBESMap)
		{
			return _activityToBESMap.get(activityid);
		}
	}
	
	synchronized static public void loadAllInstances(
		DatabaseConnectionPool connectionPool) throws SQLException
	{
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		if (connectionPool == null)
			throw new IllegalArgumentException("connectionPool argument cannot be null.");
		
		if (_connectionPool != null)
			throw new IllegalStateException("BES instances already loaded.");
		
		_connectionPool = connectionPool;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
				"SELECT besid, userloggedinaction, screensaverinactiveaction " +
					"FROM bespolicytable");
			while (rs.next())
			{
				String besid = rs.getString(1);
				BESPolicy policy = new BESPolicy(
					BESPolicyActions.valueOf(rs.getString(2)),
					BESPolicyActions.valueOf(rs.getString(3)));
				BES bes = new BES(besid, policy);
				_knownInstances.put(besid, bes);
				bes.reloadAllActivities(connection);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized static public BES getBES(String besid)
	{
		if (_connectionPool == null)
			throw new IllegalStateException("BES instances not initialized.");
		
		return _knownInstances.get(besid);
	}
	
	synchronized static public BES createBES(String besid,
		BESPolicy initialPolicy) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO bespolicytable " +
					"(besid, userloggedinaction, screensaverinactiveaction) " +
				"VALUES (?, ?, ?)");
			stmt.setString(1, besid);
			stmt.setString(2, initialPolicy.getUserLoggedInAction().name());
			stmt.setString(3, 
				initialPolicy.getScreenSaverInactiveAction().name());
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to update database table for bes creation.");
			connection.commit();
			
			BES bes;
			_knownInstances.put(besid, bes = new BES(besid, initialPolicy));
			return bes;
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized static public void deleteBES(String besid)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		BES bes = _knownInstances.get(besid);
		if (bes == null)
			throw new IllegalStateException("BES \"" + besid + "\" is unknown.");
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"DELETE FROM bespolicytable WHERE besid = ?");
			stmt.setString(1, besid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to update database table for bes deletion.");
			connection.commit();
			
			_knownInstances.remove(besid);
			bes.delete();
			StreamUtils.close(bes);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private String _besid;
	private BESPolicyEnactor _enactor;
	private HashMap<String, BESActivity> _containedActivities =
		new HashMap<String, BESActivity>();
	
	private BES(String besid, BESPolicy policy)
	{
		_besid = besid;
		_enactor = new BESPolicyEnactor(policy);
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public boolean isAcceptingActivites()
	{
		return !_enactor.getCurrentAction().equals(BESPolicyActions.KILL);
	}
	
	synchronized public Collection<BESActivity> getContainedActivities()
	{
		return new ArrayList<BESActivity>(
			_containedActivities.values());
	}
	
	synchronized private void delete() throws SQLException
	{
		for (String activity : _containedActivities.keySet())
		{
			try
			{
				deleteActivity(activity);
			}
			catch (UnknownActivityIdentifierFaultType uaift)
			{
				_logger.error(
					"Unexpected exception thrown while deleting activity.", 
					uaift);
			}
		}
	}
	
	synchronized private String findJobName(String suggestedJobName)
	{
		if (suggestedJobName == null)
        {
            SimpleDateFormat format = new SimpleDateFormat(
                "ddMMMMyyyy.HHmm.ss");
            suggestedJobName = format.format(new Date());
        }

        int count = 0;

        Pattern pattern = Pattern.compile(
            "^" + Pattern.quote(suggestedJobName) + " ([0-9]+)$");

        for (BESActivity activity : _containedActivities.values())
        {
            String name = activity.getJobName();
            if (name.equals(suggestedJobName) && count == 0)
                count = 1;
            else
            {
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches())
                {
                    int newVal = Integer.parseInt(matcher.group(1));
                    if (count <= newVal)
                        count = newVal + 1;
                }
            }
        }

        if (count != 0)
            return String.format("%s %d", suggestedJobName, count);

        return suggestedJobName;
	}
	
	synchronized public BESActivity createActivity(
		String activityid,
		JobDefinition_Type jsdl, Collection<Identity> owners,
		ICallingContext callingContext, BESWorkingDirectory activityCWD,
		Vector<ExecutionPhase> executionPlan, 
		EndpointReferenceType activityEPR, 
		String activityServiceName, String suggestedJobName) 
			throws SQLException, ResourceException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		String jobName = findJobName(suggestedJobName);
		ActivityState state = new ActivityState(
			ActivityStateEnumeration.Pending, null, false);
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO besactivitiestable " +
					"(activityid, besid, jsdl, owners, callingcontext, " +
					"state, submittime, suspendrequested, " +
					"terminaterequested, activitycwd, executionplan, " +
					"nextphase, activityepr, activityservicename, jobname) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, activityid);
			stmt.setString(2, _besid);
			stmt.setBlob(3, DBSerializer.xmlToBlob(jsdl));
			stmt.setBlob(4, DBSerializer.toBlob(owners));
			stmt.setBlob(5, DBSerializer.toBlob(callingContext));
			stmt.setBlob(6, DBSerializer.toBlob(state));
			stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
			stmt.setShort(8, (short)0);
			stmt.setShort(9, (short)0);
			stmt.setString(10, String.format("%s%s", 
				activityCWD.mustDelete() ? "d" : "k",
				activityCWD.getWorkingDirectory().getAbsolutePath()));
			stmt.setBlob(11, DBSerializer.toBlob(executionPlan));
			stmt.setInt(12, 0);
			stmt.setBlob(13, EPRUtils.toBlob(activityEPR));
			stmt.setString(14, activityServiceName);
			stmt.setString(15, jobName);
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to update database for bes activity creation.");
			connection.commit();
			
			PostTargets.poster().post(
				JobEvent.activityCreated(callingContext,
				activityid));
			
			BESActivity activity = new BESActivity(_connectionPool, 
				this, activityid, state, activityCWD, executionPlan,
				0, activityServiceName, jobName, false, false);
			_containedActivities.put(activityid, activity);
			addActivityToBESMapping(activityid, this);
			return activity;
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public BESActivity findActivity(String activityid) 
	{
		return _containedActivities.get(activityid);
	}
	
	synchronized public void deleteActivity(String activityid)
		throws UnknownActivityIdentifierFaultType, SQLException
	{
		BESActivity activity = _containedActivities.get(activityid);
		if (activity == null)
			throw new UnknownActivityIdentifierFaultType(
				"Couldn't find activity \"" + activityid + "\".", null);
		StreamUtils.close(activity);
		
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"DELETE FROM besactivitiestable WHERE activityid = ?");
			stmt.setString(1, activityid);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			stmt = connection.prepareStatement(
				"DELETE FROM besactivitypropertiestable WHERE activityid = ?");
			stmt.setString(1, activityid);
			stmt.executeUpdate();
			connection.commit();
			
			_containedActivities.remove(activityid);
			removeActivityToBESMapping(activityid);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public void close() throws IOException
	{
		if (_enactor != null)
			_enactor.close();
	}
	
	public BESPolicyEnactor getPolicyEnactor()
	{
		return _enactor;
	}
	
	@SuppressWarnings("unchecked")
	synchronized private void reloadAllActivities(Connection connection)
		throws SQLException
	{
		PreparedStatement queryStmt = null;
		ResultSet rs = null;
		
		try
		{
			queryStmt = connection.prepareStatement(
				"SELECT activityid, state, suspendrequested, " +
					"terminaterequested, activitycwd, executionplan, " +
					"nextphase, activityservicename, jobname " +
				"FROM besactivitiestable WHERE besid = ?");
			
			queryStmt.setString(1, _besid);
			rs = queryStmt.executeQuery();
			while (rs.next())
			{
				String activityid = rs.getString(1);
				ActivityState state = (ActivityState)DBSerializer.fromBlob(
					rs.getBlob(2));
				boolean suspendRequested = 
					(rs.getShort(3) == 0) ? false : true;
				boolean terminateRequested = 
					(rs.getShort(4) == 0) ? false : true;
				
				
				String activityCWDString = rs.getString(5);
				BESWorkingDirectory activityCWD;
				
				if (activityCWDString.startsWith("d|"))
					activityCWD = new BESWorkingDirectory(
						new File(activityCWDString.substring(2)), true);
				else
					activityCWD = new BESWorkingDirectory(
						new File(activityCWDString.substring(2)), false);
				
				Vector<ExecutionPhase> executionPlan = 
					(Vector<ExecutionPhase>)DBSerializer.fromBlob(
						rs.getBlob(6));
				int nextPhase = rs.getInt(7);
				String activityServiceName = rs.getString(8);
				String jobName = rs.getString(9);
								
				BESActivity activity = new BESActivity(_connectionPool,
					this, activityid, state, activityCWD, executionPlan, 
					nextPhase, activityServiceName, jobName, suspendRequested, 
					terminateRequested);
				_containedActivities.put(activityid, activity);
				addActivityToBESMapping(activityid, this);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(queryStmt);
		}
	}
}