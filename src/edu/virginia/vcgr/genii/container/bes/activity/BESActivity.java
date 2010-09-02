package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityStateChangedContents;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.BESPolicyListener;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.bes.execution.SuspendableExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;

public class BESActivity implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESActivity.class);

	private DatabaseConnectionPool _connectionPool;
	
	private boolean _finishCaseHandled = false;
	private BES _bes;
	private PolicyListener _policyListener;
	private String _activityid;
	private ActivityState _state;
	private boolean _suspendRequested;
	private boolean _terminateRequested;
	private BESWorkingDirectory _activityCWD;
	private Vector<ExecutionPhase> _executionPlan;
	private int _nextPhase;
	private String _activityServiceName;
	private String _jobName;
	private ActivityRunner _runner;
	
	public BESActivity(DatabaseConnectionPool connectionPool,
		BES bes, String activityid,
		ActivityState state, BESWorkingDirectory activityCWD, 
		Vector<ExecutionPhase> executionPlan, int nextPhase, 
		String activityServiceName, String jobName,
		boolean suspendRequested, boolean terminateRequested)
	{
		_connectionPool = connectionPool;
		
		_bes = bes;
		_activityid = activityid;
		_state = state;
		_activityCWD = activityCWD;
		_executionPlan = executionPlan;
		_nextPhase = nextPhase;
		_activityServiceName = activityServiceName;
		_jobName = jobName;
		_suspendRequested = suspendRequested;
		_terminateRequested = terminateRequested;
		
		_runner = new ActivityRunner(_suspendRequested, _terminateRequested);
		_policyListener = new PolicyListener();
		_bes.getPolicyEnactor().addBESPolicyListener(_policyListener);
		
		
		if (!handleFinishedCase())
		{	
			Thread thread = new Thread(_runner, "BES Activity Runner Thread");
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	public boolean isGood()
		throws SQLException
	{
		Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = _connectionPool.acquire(true);
            stmt = connection.prepareStatement(
                "SELECT callingcontext " +
                    "FROM besactivitiestable " +
                "WHERE activityid = ?");
            stmt.setString(1, _activityid);
            rs = stmt.executeQuery();
            if (!rs.next())
                return false;
            
            ICallingContext cctxt = (ICallingContext)DBSerializer.fromBlob(
                rs.getBlob(1));
            
            return ContextManager.isGood(cctxt);
        }
        finally
        {
            StreamUtils.close(rs);
            StreamUtils.close(stmt);
            _connectionPool.release(connection);
        }
	}
	
	public String getActivityID()
	{
		return _activityid;
	}
	
	public BESWorkingDirectory getActivityCWD()
	{
		return _activityCWD;
	}
	
	@SuppressWarnings("unchecked")
	synchronized public void verifyOwner() 
		throws GenesisIISecurityException, SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT owners FROM besactivitiestable " +
				"WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException(
					"Unable to load owner information from database " +
					"for bes activity.");
			
			if (!QueueSecurity.isOwner(
				(Collection<Identity>)DBSerializer.fromBlob(rs.getBlob(1))))
				throw new GenesisIISecurityException(
					"Caller does not have permission to get " +
					"activity status for activity \"" + _activityid + "\".");
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public EndpointReferenceType getActivityEPR()
		throws SQLException, ResourceException, NoSuchActivityFault
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"SELECT activityepr FROM besactivitiestable " +
				"WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new NoSuchActivityFault(_activityid);
			return EPRUtils.fromBlob(rs.getBlob(1));
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public JobDefinition_Type getJobDefinition()
		throws SQLException, IOException, ClassNotFoundException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"SELECT jsdl FROM besactivitiestable " +
				"WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Unable to find activity in database.");
			return DBSerializer.xmlFromBlob(JobDefinition_Type.class, 
				rs.getBlob(1));
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public Collection<Throwable> getFaults()
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		Collection<Throwable> faults = new LinkedList<Throwable>();
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"SELECT fault FROM besactivityfaultstable " +
				"WHERE besactivityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				faults.add((Throwable)DBSerializer.fromBlob(rs.getBlob(1)));
			}
			
			return faults;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_policyListener != null)
		{
			_bes.getPolicyEnactor().removeBESPolicyListener(_policyListener);
			_policyListener = null;
		}
			
		try
		{
			terminate();
		}
		catch (Throwable ee)
		{
			_logger.error("Problem trying to early terminate activity.", 
				ee);
		}
	}
	
	public String getJobName()
	{
		return _jobName;
	}
	
	synchronized public void suspend() throws ExecutionException,
		SQLException
	{
		if (_suspendRequested)
			return;
		
		updateState(true, _terminateRequested);
		if (_runner != null)
			_runner.requestSuspend();
	}
	
	synchronized public void terminate() throws ExecutionException,
		SQLException
	{
		if (_terminateRequested)
			return;
		
		updateState(false, true);
		if (_runner != null)
			_runner.requestTerminate(false);
	}
	
	synchronized public void resume() throws ExecutionException,
		SQLException
	{
		if (!_suspendRequested)
			return;
		
		updateState(false, _terminateRequested);
		if (_runner != null)
			_runner.requestResume();
	}
	
	synchronized public ActivityState getState()
	{
		ActivityState retState = (ActivityState)_state.clone();
		if (_runner != null && _runner.isSuspended())
			retState.suspend(true);
		
		return retState;
	}
	
	synchronized private void updateState(
		boolean suspendRequested, boolean terminateRequested)
			throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"UPDATE besactivitiestable " +
					"SET suspendrequested = ?, terminaterequested = ? " +
				"WHERE activityid = ?");
			stmt.setShort(1, suspendRequested ? (short)1 : (short)0);
			stmt.setShort(2, terminateRequested ? (short)1 : (short)0);
			stmt.setString(3, _activityid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database.");
			connection.commit();
			
			_suspendRequested = suspendRequested;
			_terminateRequested = terminateRequested;
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized private void updateState(int nextPhase, ActivityState state)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"UPDATE besactivitiestable SET nextphase = ?, state = ? " +
				"WHERE activityid = ?");
			stmt.setInt(1, nextPhase);
			stmt.setBlob(2, DBSerializer.toBlob(state, 
				"besactivitiestable", "state"));
			stmt.setString(3, _activityid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database.");
			connection.commit();
			
			_nextPhase = nextPhase;
			_state = state;
			
			notifyStateChange();
		}
		catch (ResourceException e)
		{
			_logger.warn("Unable to send notification for status change.", e);
		}
		catch (ResourceUnknownFaultType e)
		{
			_logger.warn("Unable to send notification for status change.", e);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private WorkingContext createWorkingContext()
		throws SQLException
	{
		Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = _connectionPool.acquire(true);
            stmt = connection.prepareStatement(
                "SELECT callingcontext, activityepr " +
                    "FROM besactivitiestable " +
                "WHERE activityid = ?");
            stmt.setString(1, _activityid);
            rs = stmt.executeQuery();
            if (!rs.next())
                throw new SQLException("Activity \"" + _activityid +
                    "\" does not exist.");
            ICallingContext cctxt = (ICallingContext)DBSerializer.fromBlob(
                rs.getBlob(1));
            EndpointReferenceType epr = EPRUtils.fromBlob(rs.getBlob(2));

            WorkingContext ret = new WorkingContext();
            ret.setProperty(WorkingContext.EPR_PROPERTY_NAME, epr);
            ret.setProperty(WorkingContext.TARGETED_SERVICE_NAME, 
            	_activityServiceName);
            ret.setProperty(WorkingContext.CURRENT_CONTEXT_KEY, cctxt);
            return ret;
        }
        catch (ResourceException re)
        {
            throw new SQLException("Unable to load working context.", re);
        }
        finally
        {
            StreamUtils.close(rs);
            StreamUtils.close(stmt);
            _connectionPool.release(connection);
        }
	}
	
	private void execute(ExecutionPhase phase) throws Throwable
	{
		WorkingContext ctxt = createWorkingContext();
		
		try
		{
			WorkingContext.setCurrentWorkingContext(ctxt);
			phase.execute(getExecutionContext());
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
	}
	
	private void notifyStateChange()
		throws ResourceException, ResourceUnknownFaultType, SQLException
	{
		WorkingContext ctxt = createWorkingContext();
		ActivityState state = getState();
		
		try
		{
			WorkingContext.setCurrentWorkingContext(ctxt);
			TopicSet space = TopicSet.forPublisher(BESActivityServiceImpl.class);
			TopicPath topicPath;
			
			if (state.isFinalState())
				topicPath =
					BESActivityServiceImpl.ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC;
			else
				topicPath =
					BESActivityServiceImpl.ACTIVITY_STATE_CHANGED_TOPIC;
			
			PublisherTopic topic = space.createPublisherTopic(topicPath);
			topic.publish(new BESActivityStateChangedContents(state));
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
	}
	
	private ExecutionContext getExecutionContext()
	{
		return new ExecutionContext()
		{
			@Override
			public void updateState(ActivityState newState)
			{
				_state = (ActivityState)newState.clone();
			}
			
			@Override
			public ICallingContext getCallingContext()
					throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
				
				try
				{
					connection = _connectionPool.acquire(true);
					stmt = connection.prepareStatement(
						"SELECT callingcontext FROM besactivitiestable " +
						"WHERE activityid = ?");
					stmt.setString(1, _activityid);
					rs = stmt.executeQuery();
					if (!rs.next())
						throw new SQLException(
							"Unable to find activity in database.");
					return (ICallingContext)DBSerializer.fromBlob(rs.getBlob(1));
				}
				catch (SQLException sqe)
				{
					throw new ExecutionException(
						"Database error trying to get calling context.", sqe);
				}
				finally
				{
					StreamUtils.close(rs);
					StreamUtils.close(stmt);
					_connectionPool.release(connection);
				}
			}
			
			public String getBESEPI()
			{
				return _bes.getBESEPI();
			}

			@Override
			public BESWorkingDirectory getCurrentWorkingDirectory()
				throws ExecutionException
			{
				return _activityCWD;
			}
			
			@Override
			public Serializable getProperty(String name)
					throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;
				
				try
				{
					connection = _connectionPool.acquire(true);
					stmt = connection.prepareStatement(
						"SELECT propertyvalue FROM besactivitypropertiestable " +
						"WHERE activityid = ? AND propertyname = ?");
					stmt.setString(1, _activityid);
					stmt.setString(2, name);
					
					rs = stmt.executeQuery();
					if (!rs.next())
						return null;
					
					return (Serializable)DBSerializer.fromBlob(rs.getBlob(1));
				}
				catch (SQLException sqe)
				{
					throw new ExecutionException(
						"Database error trying to get activity property.", sqe);
				}
				finally
				{
					StreamUtils.close(rs);
					StreamUtils.close(stmt);
					_connectionPool.release(connection);
				}
			}

			@Override
			public void setProperty(String name, Serializable value)
					throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;
				
				try
				{
					connection = _connectionPool.acquire(false);
					stmt = connection.prepareStatement(
						"DELETE FROM besactivitypropertiestable " +
						"WHERE activityid = ? AND propertyname = ?");
					stmt.setString(1, _activityid);
					stmt.setString(2, name);
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
					if (value != null)
					{
						stmt = connection.prepareStatement(
							"INSERT INTO besactivitypropertiestable " +
							"VALUES (?, ?, ?)");
						
						stmt.setString(1, _activityid);
						stmt.setString(2, name);
						stmt.setBlob(3, DBSerializer.toBlob(value,
							"besactivitypropertiestable", "propertyvalue"));
						stmt.executeUpdate();
					}
					
					connection.commit();
				}
				catch (SQLException sqe)
				{
					throw new ExecutionException(
						"Database error trying to get activity property.", sqe);
				}
				finally
				{
					StreamUtils.close(stmt);
					_connectionPool.release(connection);
				}
			}
		};
	}
	
	private void addFault(Throwable cause, int attemptsLeft)
	{
		if (attemptsLeft <= 0)
			return;
		
		Connection connection = null;
		PreparedStatement stmt = null;
		Blob blob = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			
			try
			{
				blob = DBSerializer.toBlob(cause,
					"besactivityfaultstable", "fault");
			}
			catch (SQLException sqe)
			{
				_logger.error("Attempt to serialize an unserializable " +
					"exception into the database.", cause);
				addFault(new Exception(
					"Unserializable fault occurred in BES activity (" + 
					cause.getLocalizedMessage() + 
					") -- no further information available."), attemptsLeft - 1);
					return;
			}
			
			stmt = connection.prepareStatement(
				"INSERT INTO besactivityfaultstable " +
					"(besactivityid, fault) " +
				"VALUES(?, ?)");
			stmt.setString(1, _activityid);
			stmt.setBlob(2, blob);
			stmt.executeUpdate();
			connection.commit();
		}
		catch (Throwable cause2)
		{
			_logger.error("Unexpected error while adding fault " +
				"to bes activity fault database.", cause2);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private void cleanupUnnecessaryMemory()
	{
		// Now that we are done running, we should free up any memory that
		// we no longer need to use.
		
		/* These either can't be free'd (they aren't objects), or they are
		 * merely references to objects that are held in other places:
			_connectionPool;
			_suspendRequested;
			_terminateRequested;
			_nextPhase;
			_bes
		*/
		
		if (_policyListener != null)
		{
			_bes.getPolicyEnactor().removeBESPolicyListener(_policyListener);
			_policyListener = null;
		}
			
		_executionPlan.clear();
		_executionPlan = null;
		_runner = null;
		_activityServiceName = null;
		
		/* And these we may need later:
		 * _activityid
		 * _state
		 * _jobName;
		 * _activityCWD = null;
		 */
	}
	
	final private boolean finishedExecution()
	{
		return _nextPhase >= _executionPlan.size();
	}
	
	private boolean containsIgnoreableFault(Collection<Throwable> faults)
	{
		for (Throwable fault : faults)
		{
			if (fault instanceof IgnoreableFault)
				return true;
		}
		
		return false;
	}
	
	private boolean handleFinishedCase()
	{
		if (finishedExecution())
		{
			synchronized(this)
			{
				if (_finishCaseHandled)
					return true;
				_finishCaseHandled = true;
			}
			
			try
			{
				Collection<Throwable> faults = getFaults();
				if (getFaults().size() > 0)
				{
					if (!containsIgnoreableFault(faults))
					{
						updateState(_executionPlan.size(),
							new ActivityState(
								ActivityStateEnumeration.Failed, 
								null, false));
					} else
					{
						updateState(_executionPlan.size(),
							new ActivityState(
								ActivityStateEnumeration.Failed,
								"Ignoreable", false));
					}
				} else
					updateState(_executionPlan.size(),
						new ActivityState(
							ActivityStateEnumeration.Finished, 
							null, false));
			}
			catch (SQLException cause)
			{
				_logger.error("BES Activity Unrecoverably Faulted.", cause);
				addFault(cause, 3);
				try
				{
					updateState(_executionPlan.size(),
						new ActivityState(ActivityStateEnumeration.Failed, 
							null, false));
				}
				catch (Throwable cause2)
				{
					_logger.error(
						"Unexpected exception occured in bes activity.", 
						cause2);
					return true;
				}
			}
			finally
			{
				cleanupUnnecessaryMemory();
			}
			
			return true;
		} else
			return false;
	}
	
	private class ActivityRunner implements Runnable
	{
		private boolean _terminateRequested = false;
		private boolean _suspendRequested = false;
		
		private boolean _suspended = false;
		
		private Object _phaseLock;
		private ExecutionPhase _currentPhase = null;
		
		public ActivityRunner(boolean suspendRequested, 
			boolean terminateRequested)
		{
			_phaseLock = BESActivity.this;
			_terminateRequested = terminateRequested;
			_suspendRequested = suspendRequested;
		}
		
		final public boolean isSuspended()
		{
			synchronized(_phaseLock)
			{
				return _suspended ||
					(_currentPhase != null && 
						_currentPhase instanceof SuspendableExecutionPhase);
			}
		}
		
		public boolean requestSuspend() throws ExecutionException
		{
			synchronized(_phaseLock)
			{
				if (_suspendRequested || _terminateRequested)
					return true;
				
				_suspendRequested = true;
				if (_currentPhase != null)
				{
					if (_currentPhase instanceof SuspendableExecutionPhase)
						((SuspendableExecutionPhase)_currentPhase).suspend();
					else
						return false;
				} else
					return true;
			}

			return true;
		}
		
		public void requestTerminate(boolean countAsFailedAttempt)
			throws ExecutionException
		{
			synchronized(_phaseLock)
			{
				if (_terminateRequested)
					return;
				
				_suspendRequested = false;
				_terminateRequested = true;
				
				if (_currentPhase != null)
				{
					if (_currentPhase instanceof TerminateableExecutionPhase)
						((TerminateableExecutionPhase)_currentPhase).terminate(
							countAsFailedAttempt);
				} else
				{
					_phaseLock.notify();
				}
			}
		}
		
		public void requestResume() throws ExecutionException
		{
			synchronized(_phaseLock)
			{
				if (!_suspendRequested && !_suspended)
					return;
				
				_suspendRequested = false;
				
				if (_currentPhase != null)
				{
					if (_currentPhase instanceof SuspendableExecutionPhase)
						((SuspendableExecutionPhase)_currentPhase).resume();
				}
				
				_phaseLock.notify();
			}
		}
		
		public void run()
		{
			while (true)
			{
				try
				{
					synchronized(_phaseLock)
					{
						if (handleFinishedCase())
							break;
					
						if (_terminateRequested)
						{
							updateState(_executionPlan.size(), new ActivityState(
								ActivityStateEnumeration.Cancelled, null, false));
							break;
						}
	
						while (_suspendRequested)
						{
							_suspended = true;
							try
							{
								_phaseLock.wait();
							}
							catch (InterruptedException ie)
							{
								Thread.currentThread().isInterrupted();
							}
						}
						_suspended = false;
						
						if (_terminateRequested)
						{
							updateState(_executionPlan.size(), new ActivityState(
								ActivityStateEnumeration.Cancelled, null, false));
							break;
						}
						
						_currentPhase = _executionPlan.get(_nextPhase);
						updateState(_nextPhase, _currentPhase.getPhaseState());
					}
				
					try
					{
						execute(_currentPhase);
					}
					catch (ContinuableExecutionException cee)
					{
						addFault(cee, 3);
					}
					catch (InterruptedException ie)
					{
						Thread.currentThread().isInterrupted();
					}
					
					synchronized(_phaseLock)
					{
						if (_terminateRequested)
							continue;
						
						_currentPhase = null;
						updateState(_nextPhase + 1, _state);
					}
				}
				catch (Throwable cause)
				{
					_logger.error("BES Activity Unrecoverably Faulted.", cause);
					addFault(cause, 3);
					try
					{
						updateState(_executionPlan.size(),
							new ActivityState(ActivityStateEnumeration.Failed, 
								null, false));
					}
					catch (Throwable cause2)
					{
						_logger.error(
							"Unexpected exception occured in bes activity.", 
							cause2);
						return;
					}
				}
			}
		}
	}
	
	private class PolicyListener implements BESPolicyListener
	{
		@Override
		public void kill() throws ExecutionException
		{
			_runner.requestTerminate(true);
		}

		@Override
		public void resume() throws ExecutionException
		{
			_runner.requestResume();
		}

		@Override
		public void suspend() throws ExecutionException
		{
			_runner.requestSuspend();
		}

		@Override
		public void suspendOrKill() throws ExecutionException
		{
			if (!_runner.requestSuspend())
				kill();
		}
	}
}
