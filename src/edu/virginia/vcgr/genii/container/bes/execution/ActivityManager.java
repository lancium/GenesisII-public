package edu.virginia.vcgr.genii.container.bes.execution;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import edu.virginia.vcgr.genii.client.machine.MachineFacetUpdater;
import edu.virginia.vcgr.genii.client.machine.MachineListener;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;
import edu.virginia.vcgr.genii.container.bes.BESPolicyActions;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;

public class ActivityManager
{
	static private Log _logger = LogFactory.getLog(ActivityManager.class);
	
	static private ActivityManager _manager = null;
	
	synchronized static public void startManager(
		DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		if (_manager != null)
			throw new IllegalStateException(
				"Activity manager already started.");
		
		_manager = new ActivityManager(connectionPool);
	}
	
	synchronized static public ActivityManager getManager()
	{
		if (_manager == null)
			throw new IllegalStateException(
				"Activity manager not started yet.");
		
		return _manager;
	}
	
	private BESPolicyEnactor _enactor;
	private DatabaseConnectionPool _connectionPool;
	private HashMap<String, Activity> _activities =
		new HashMap<String, Activity>();
	private HashMap<String, BESPolicy> _besPolicies =
		new HashMap<String, BESPolicy>();
	
	private void loadBESPolicies(Connection connection)
		throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
				"SELECT besid, userloggedinaction, screensaverinactiveaction " +
				"FROM bespolicytable");
			while (rs.next())
			{
				_besPolicies.put(rs.getString(1), 
					new BESPolicy(
						BESPolicyActions.valueOf(rs.getString(2)),
						BESPolicyActions.valueOf(rs.getString(3))));
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	@SuppressWarnings("unchecked")
	private ActivityManager(DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		_connectionPool = connectionPool;
		
		Connection connection = null;
		Statement stmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		int updateCount = 0;
		
		try
		{
			connection = _connectionPool.acquire();
			loadBESPolicies(connection);
			
			_enactor = new BESPolicyEnactor();
			
			updateStmt = connection.prepareStatement(
				"UPDATE besactivitiestable " +
					"SET suspendrequested = 0, " +
					"terminaterequested = 0, " +
					"state = ? " +
				"WHERE activityid = ?");
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
				"SELECT activityid, besid, state, suspendrequested, " +
					"terminaterequested, activitycwd, executionplan, " +
					"nextphase, jobname FROM besactivitiestable");
			while (rs.next())
			{
				String jobname = rs.getString(9);
				String activityid = rs.getString(1);
				String besid = rs.getString(2);
				ActivityState state = (ActivityState)DBSerializer.fromBlob(
					rs.getBlob(3));
				boolean sReq = rs.getShort(4) != 0;
				boolean tReq = rs.getShort(5) != 0;
				File activityCWD = new File(rs.getString(6));
				Vector<ExecutionPhase> phases = 
					(Vector<ExecutionPhase>)DBSerializer.fromBlob(
						rs.getBlob(7));
				int nextPhase = rs.getInt(8);
				
				if (sReq || tReq)
				{
					updateCount++;
					if (sReq)
						state.suspend(true);
					if (tReq)
						state = new ActivityState(
							ActivityStateEnumeration.Cancelled, null, false);
					updateStmt.setBlob(1, DBSerializer.toBlob(state));
					updateStmt.setString(2, activityid);
					updateStmt.addBatch();
				}
				
				Activity activity = new Activity(
					_connectionPool, jobname, besid, activityid, activityCWD,
					state, phases, nextPhase, false);
				_activities.put(activityid, activity);
			}
			
			if (updateCount > 0)
				updateStmt.executeBatch();
			connection.commit();
			
			for (Activity activity : _activities.values())
				activity.start();
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(updateStmt);
			_connectionPool.release(connection);
		}
	}
	
	public void removeBESPolicy(String besid)
	{
		synchronized(_besPolicies)
		{
			_besPolicies.remove(besid);
		}
	}
	
	public void setBESPolicy(String besid, BESPolicy policy)
	{
		synchronized(_besPolicies)
		{
			_besPolicies.put(besid, policy);
		}
	}
	
	public Activity findActivity(String besid, String activityid, 
		boolean checkCaller)
			throws UnknownActivityIdentifierFaultType, AuthZSecurityException
	{
		Activity activity;
		
		synchronized(_activities)
		{
			activity = _activities.get(activityid);
		}
		
		if (activity == null || (!activity.getBESID().equals(besid)))
			throw new UnknownActivityIdentifierFaultType(
				"Unknown activity idetnifer \"" + activityid + "\".", null);
		
		if (checkCaller)
		{
			try
			{
				Collection<Identity> owners = activity.getOwners();
				if (!QueueSecurity.isOwner(owners))
					throw new AuthZSecurityException("Not permitted to get activity.");
			}
			catch (SQLException sqe)
			{
				throw new UnknownActivityIdentifierFaultType(
					"Unknown activity identifier \"" + activityid + "\".", 
					null);
			}
		}
		
		return activity;
	}
	
	public Activity findActivity(String activity)
	{
		synchronized(_activities)
		{
			return _activities.get(activity);
		}
	}
	
	public Collection<Activity> getAllActivities(String besid)
	{
		Collection<Activity> ret = new LinkedList<Activity>();
		
		synchronized(_activities)
		{
			for (Activity activity : _activities.values())
			{
				if (activity.getBESID().equals(besid))
					ret.add(activity);
			}
		}
		
		return ret;
	}
	
	synchronized public String findJobName(String val)
	{
		if (val == null)
		{
			SimpleDateFormat format = new SimpleDateFormat(
				"ddMMMMyyyy.HHmm.ss");
			val = format.format(new Date());
		}
		
		int count = 0;
	
		Pattern pattern = Pattern.compile(
			"^" + Pattern.quote(val) + " ([0-9]+)$"); 
		
		for (Activity activity : _activities.values())
		{
			String name = activity.getName();
			if (name.equals(val) && count == 0)
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
			return String.format("%s %d", val, count);
		
		return val;
	}
	
	synchronized public void createActivity(
		String jobName,
		String besid, String activityid,
		EndpointReferenceType activityEPR,
		String activityServiceName,
		JobDefinition_Type jsdl, Collection<Identity> owners,
		ICallingContext callingContext, File activityCWD,
		Vector<ExecutionPhase> phases)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		jobName = findJobName(jobName);
		
		ActivityState state = new ActivityState(
			ActivityStateEnumeration.Pending, null, false);
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO besactivitiestable (activityid, besid, jsdl, " +
					"owners, callingcontext, state, suspendrequested, " +
					"terminaterequested, activitycwd, executionplan, " +
					"nextphase, submittime, activityepr, " +
					"activityservicename, jobname) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, activityid);
			stmt.setString(2, besid);
			stmt.setBlob(3, DBSerializer.xmlToBlob(jsdl));
			stmt.setBlob(4, DBSerializer.toBlob(owners));
			stmt.setBlob(5, DBSerializer.toBlob(callingContext));
			stmt.setBlob(6, DBSerializer.toBlob(state));
			stmt.setShort(7, (short)0);
			stmt.setShort(8, (short)0);
			stmt.setString(9, activityCWD.getAbsolutePath());
			stmt.setBlob(10, DBSerializer.toBlob(phases));
			stmt.setInt(11, 0);
			stmt.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
			stmt.setBlob(13, EPRUtils.toBlob(activityEPR));
			stmt.setString(14, activityServiceName);
			stmt.setString(15, jobName);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to store job in database.");
			connection.commit();
			
			Activity activity = new Activity(
				_connectionPool, jobName, besid, activityid, activityCWD, 
				state, phases, 0, false);
			synchronized(_activities)
			{
				_activities.put(activityid, activity);
			}
			
			activity.start();
		}
		catch (ResourceException re)
		{
			throw new SQLException("Unable to serialize epr.", re);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public void deleteActivity(Activity activity)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			activity.close();
			
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"DELETE FROM besactivitiestable WHERE activityid = ?");
			stmt.setString(1, activity.getActivityID());
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			stmt = connection.prepareStatement(
				"DELETE FROM besactivityfaultstable WHERE besactivityid = ?");
			stmt.setString(1, activity.getActivityID());
			stmt.executeUpdate();
			
			connection.commit();
			
			synchronized(_activities)
			{
				_activities.remove(activity.getActivityID());
			}
		}
		catch (IOException ioe)
		{
			throw new SQLException("Unable to destroy activity.", ioe);
		}
	}
	
	synchronized public void deleteBES(String besid)
	{
		Collection<Activity> activities =
			new LinkedList<Activity>();
		synchronized(_activities)
		{
			for (Activity activity : _activities.values())
			{
				if (activity.getBESID().equals(besid))
					activities.add(activity);
			}
		}
		
		for (Activity activity : activities)
		{
			try
			{
				deleteActivity(activity);
			}
			catch(Throwable cause)
			{
				_logger.error("Error trying to destroy activity on bes.", 
					cause);
			}
		}
	}
	
	synchronized private void enactPolicy(Activity activity, 
		BESPolicyActions action) throws SQLException, ExecutionException
	{
		if (action.equals(BESPolicyActions.NOACTION))
		{
			activity.containerResume();
		} else if (action.equals(BESPolicyActions.SUSPEND))
		{
			activity.containerSuspend();
		} else if (action.equals(BESPolicyActions.SUSPENDORKILL))
		{
			activity.containerSuspend();
			if (activity.getState().isSuspended())
				return;
			action = BESPolicyActions.KILL;
		}
		
		if (action.equals(BESPolicyActions.KILL))
			activity.terminateActivity();
	}
	
	synchronized private void enactPolicyChange()
	{
		HashMap<String, BESPolicyActions> actions = 
			new HashMap<String, BESPolicyActions>();
		
		if (_enactor == null)
		{
			// During startup still
			return;
		}
		
		synchronized(_besPolicies)
		{
			for (String besid : _besPolicies.keySet())
			{
				actions.put(besid, _enactor.getCurrentAction(besid));
			}
		}
		
		synchronized(_activities)
		{
			for (Activity activity : _activities.values())
			{
				try
				{
					BESPolicyActions action = actions.get(activity.getBESID());
					enactPolicy(activity, action);
				}
				catch (Throwable t)
				{
					_logger.error(
						"Exception thrown while enacting BES policy.", t);
				}
			}
		}
	}
	
	private class BESPolicyEnactor implements MachineListener
	{
		private MachineFacetUpdater _updater;
		private Boolean _userLoggedIn = null;
		private Boolean _screenSaverActive = null;
		
		public BESPolicyEnactor()
		{
			_updater = new MachineFacetUpdater(1000 * 30, 1000 * 30);
			_updater.addMachineListener(this);
		}
		
		public BESPolicyActions getCurrentAction(String besid)
		{
			BESPolicy policy;
			
			synchronized(_besPolicies)
			{
				policy = _besPolicies.get(besid);
			}
			
			return policy.getCurrentAction(_userLoggedIn, _screenSaverActive);
		}
		
		@Override
		public void screenSaverActivated(boolean activated)
		{
			_screenSaverActive = new Boolean(activated);
			enactPolicyChange();
		}

		@Override
		public void userLoggedIn(boolean loggedIn)
		{
			_userLoggedIn = new Boolean(loggedIn);
			enactPolicyChange();
		}
	}
}