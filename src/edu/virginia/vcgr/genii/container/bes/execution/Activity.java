package edu.virginia.vcgr.genii.container.bes.execution;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
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
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class Activity implements Closeable
{
	static private Log _logger = LogFactory.getLog(Activity.class);
	
	private DatabaseConnectionPool _connectionPool;
	
	private Object _lockObject = new Object();
	
	private String _name;
	private String _activityid;
	private String _besid;
	private File _cwd;
	private boolean _containerSuspendRequested;
	private boolean _containerSuspended;
	
	private boolean _activitySuspendRequested;
	private boolean _activityTerminateRequested;
	private ActivityState _state;
	private Vector<ExecutionPhase> _phases;
	private ExecutionPhase _currentPhase = null;
	private int _nextPhase;
	
	private Thread _myThread = null;
	
	public Activity(DatabaseConnectionPool connectionPool,
		String name, String besid, String activityid, 
		File cwd, ActivityState state, Vector<ExecutionPhase> phases, 
		int nextPhase, boolean startContainerSuspended)
	{
		_connectionPool = connectionPool;
	
		_name = name;
		_activityid = activityid;
		_besid = besid;
		_cwd = cwd;
		_state = state;
		_phases = phases;
		_nextPhase = nextPhase;
		
		_containerSuspendRequested = startContainerSuspended;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getActivityID()
	{
		return _activityid;
	}
	
	public String getBESID()
	{
		return _besid;
	}
	
	synchronized public void start()
	{
		if (!_state.isFinalState())
		{
			_myThread = new Thread(new ActivityRunner(), "BES Activity Runner");
			_myThread.setDaemon(true);
			_myThread.start();
		} else
			_myThread = null;
	}
	
	synchronized public void close() throws IOException
	{
		FileUtils.recursivelyRemove(_cwd);
		
		if (_myThread == null)
			return;
		
		try
		{
			terminateActivity();
		}
		catch (ExecutionException ee)
		{
			_logger.error("Problem trying to terminate activity for close.",
				ee);
		}
		catch (SQLException sqe)
		{
			_logger.error("Problem trying to terminate activity for close.",
				sqe);
		}
		
		_myThread.interrupt();
		_myThread = null;
	}
	
	public void containerSuspend()
		throws ExecutionException
	{
		synchronized(_lockObject)
		{
			_containerSuspendRequested = true;
			if (_currentPhase != null &&
				(_currentPhase instanceof SuspendableExecutionPhase))
			{
				((SuspendableExecutionPhase)_currentPhase).suspend();
				_containerSuspendRequested = false;
				_containerSuspended = true;
			}
		}
	}
	
	public void containerResume()
	{
		synchronized(_lockObject)
		{
			_containerSuspendRequested = false;
			_lockObject.notify();
		}
	}
	
	public void suspendActivity()
		throws ExecutionException, SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		synchronized(_lockObject)
		{
			if (_state.isFinalState())
				return;
			
			if (_activitySuspendRequested)
				return;
						
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"UPDATE besactivitiestable SET suspendrequested = 1 " +
					"WHERE activityid = ?");
				stmt.setString(1, _activityid);
				if (stmt.executeUpdate() != 1)
					throw new SQLException(
						"Unable to update bes activity database.");
				connection.commit();
				
				_activitySuspendRequested = true;
				if (_currentPhase != null &&
					(_currentPhase instanceof SuspendableExecutionPhase))
					((SuspendableExecutionPhase)_currentPhase).suspend();
			}
			finally
			{
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}
	}

	public void resumeActivity()
		throws SQLException
	{
		synchronized(_lockObject)
		{
			if (_state.isFinalState())
				return;
			
			if (_activitySuspendRequested)
				_activitySuspendRequested = false;
			
			if (_state.isSuspended())
			{
				ActivityState newState = (ActivityState)_state.clone();
				newState.suspend(false);
				updateState(newState);
				_state = newState;
				
				_lockObject.notify();
			}
		}
	}
	
	public void terminateActivity()
		throws ExecutionException, SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		synchronized(_lockObject)
		{
			if (_state.isFinalState())
				return;
			
			if (_activityTerminateRequested)
				return;
						
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"UPDATE besactivitiestable SET terminaterequested = 1 " +
					"WHERE activityid = ?");
				stmt.setString(1, _activityid);
				if (stmt.executeUpdate() != 1)
					throw new SQLException(
						"Unable to update bes activity database.");
				connection.commit();
				
				_activityTerminateRequested = true;
				if (_currentPhase != null &&
					(_currentPhase instanceof TerminateableExecutionPhase))
				((TerminateableExecutionPhase)_currentPhase).terminate();
			}
			finally
			{
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}
	}
	
	public ActivityState getState()
	{
		synchronized(_lockObject)
		{
			ActivityState state = (ActivityState)_state.clone();
			
			if (_currentPhase != null)
				return state;
			
			if (_containerSuspended)
				state.suspend(_containerSuspended);
			return state;
		}
	}
	
	public Collection<Throwable> getFaults()
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		LinkedList<Throwable> ret = new LinkedList<Throwable>();
		synchronized(_lockObject)
		{
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"SELECT fault FROM besactivityfaultstable " +
					"WHERE besactivityid = ?");
				stmt.setString(1, _activityid);
				rs = stmt.executeQuery();
				while (rs.next())
				{
					ret.add((Throwable)DBSerializer.fromBlob(rs.getBlob(1)));
				}
				
				return ret;
			}
			finally
			{
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Identity> getOwners()
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		synchronized(_lockObject)
		{
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"SELECT owners FROM besactivitiestable " +
					"WHERE activityid = ?");
				stmt.setString(1, _activityid);
				rs = stmt.executeQuery();
				if (!rs.next())
					throw new SQLException("Unable to find activity \"" + 
						_activityid + "\".");
				return (Collection<Identity>)DBSerializer.fromBlob(rs.getBlob(1));
			}
			finally
			{
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}
	}
	
	public JobDefinition_Type getJSDL()
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		synchronized(_lockObject)
		{
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"SELECT jsdl FROM besactivitiestable " +
					"WHERE activityid = ?");
				stmt.setString(1, _activityid);
				rs = stmt.executeQuery();
				if (!rs.next())
					throw new SQLException("Unable to find activity \"" + 
						_activityid + "\".");
				return (JobDefinition_Type)DBSerializer.xmlFromBlob(
					JobDefinition_Type.class, rs.getBlob(1));
			}
			catch (ClassNotFoundException cnfe)
			{
				throw new SQLException(
					"Unable to deserialize JSDL from blob.", cnfe);
			}
			catch (IOException ioe)
			{
				throw new SQLException(
					"Unable to deserialize JSDL from blob.", ioe);
			}
			finally
			{
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}
	}
	
	private void handleLazyStateChanges()
		throws SQLException
	{
		if (_state.isFinalState())
			return;
		
		if (_activityTerminateRequested)
		{
			_state = new ActivityState(
				ActivityStateEnumeration.Cancelled, null, false);
		} else if (_activitySuspendRequested)
		{
			_state.suspend(true);
		}
		
		updateState(_state);
		_activityTerminateRequested = false;
		_activitySuspendRequested = false;
	}
	
	private void updateState(ActivityState newState)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"UPDATE besactivitiestable SET state = ?, " +
					"suspendrequested = 0, terminaterequested = 0 " +
				"WHERE activityid = ?");
			stmt.setBlob(1, DBSerializer.toBlob(newState));
			stmt.setString(2, _activityid);
			stmt.executeUpdate();
			connection.commit();
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private void updateState(int nextPhase)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"UPDATE besactivitiestable SET nextphase = ? " +
				"WHERE activityid = ?");
			stmt.setInt(1, nextPhase);
			stmt.setString(2, _activityid);
			stmt.executeUpdate();
			connection.commit();
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private void addFault(Throwable cause)
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO besactivityfaultstable (besactivityid, fault) " +
					"VALUES (?, ?)");
			stmt.setString(1, _activityid);
			stmt.setBlob(2, DBSerializer.toBlob(cause));
			stmt.executeUpdate();
			connection.commit();
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public EndpointReferenceType getActivityEPR()
		throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"SELECT activityepr " +
					"FROM besactivitiestable " +
				"WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Activity \"" + _activityid +
					"\" does not exist.");
			EndpointReferenceType epr = EPRUtils.fromBlob(rs.getBlob(1));
			return epr;
		}
		catch (ResourceException re)
		{
			throw new SQLException("Unable to load activity epr.", re);
		}
		finally
		{
			StreamUtils.close(rs);
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
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"SELECT callingcontext, activityepr, activityservicename " +
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
			String serviceName = rs.getString(3);
			
			WorkingContext ret = new WorkingContext();
			ret.setProperty(WorkingContext.EPR_PROPERTY_NAME, epr);
			ret.setProperty(WorkingContext.TARGETED_SERVICE_NAME, serviceName);
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
	
	private class ActivityRunner implements Runnable
	{
		public void run()
		{
			Throwable fault = null;
			
			while (true)
			{
				if (_myThread == null)
					return;
				
				try
				{
					synchronized(_lockObject)
					{
						handleLazyStateChanges();
						
						if (_state.isFinalState())
						{
							_myThread = null;
							return;
						}
						
						if (_containerSuspendRequested)
						{
							_containerSuspendRequested = false;
							_containerSuspended = true;
						}
						while (_state.isSuspended() || _containerSuspended)
							_lockObject.wait();
						
						_containerSuspended = false;
						_containerSuspendRequested = false;
						
						if (_state.isFinalState())
						{
							_myThread = null;
							return;
						}
						
						if (_nextPhase >= _phases.size())
						{
							_currentPhase = null;
							Collection<Throwable> faults = getFaults();
							if (faults.size() > 0)
								_state = new ActivityState(
									ActivityStateEnumeration.Failed, 
									null, false);
							else
								_state = new ActivityState(
									ActivityStateEnumeration.Finished, 
									null, false);
							updateState(_state);
							_myThread = null;
							return;
						}
						
						_currentPhase = _phases.get(_nextPhase);
						updateState(_currentPhase.getPhaseState());
						_state = _currentPhase.getPhaseState();
					}
					
					WorkingContext ctxt = createWorkingContext();
					try
					{
						WorkingContext.setCurrentWorkingContext(ctxt);
						
						if (_myThread == null)
							return;
						_currentPhase.execute(new InternalExecutionContext());
						if (_myThread == null)
							return;
					}
					catch (ContinuableExecutionException cee)
					{
						_logger.warn(
							"Activity threw an exception which we can continue from.", cee);
						Throwable cause = cee.getCause();
						if (cause != null)
							addFault(cause);
						else
							addFault(cee);
					}
					catch (Throwable cause)
					{
						_logger.error(
							"Activity threw an unrecoverable exception.", cause);
						addFault(cause);
						fault = cause;
					}
					finally
					{
						WorkingContext.setCurrentWorkingContext(null);
					}
					
					synchronized(_lockObject)
					{
						if (fault != null)
						{
							ActivityState newState = new ActivityState(
								ActivityStateEnumeration.Failed, null, false);
							updateState(newState);
							_state = newState;
						} else
						{					
							_currentPhase = null;
							updateState(_nextPhase + 1);
							_nextPhase++;
						}
					}
				}
				catch (SQLException sqe)
				{
					_logger.fatal("Unable to update bes activity database.", sqe);
				}
				catch (InterruptedException ie)
				{
					Thread.currentThread().isInterrupted();
				}
			}
		}
	}
	
	private class InternalExecutionContext implements ExecutionContext
	{
		@Override
		public ICallingContext getCallingContext()
			throws ExecutionException
		{
			Connection connection = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try
			{
				connection = _connectionPool.acquire();
				stmt = connection.prepareStatement(
					"SELECT callingcontext FROM besactivitiestable " +
						"WHERE activityid = ?");
				stmt.setString(1, _activityid);
				rs = stmt.executeQuery();
				if (!rs.next())
					throw new ExecutionException(
						"Unable to find calling context.");
				return (ICallingContext)DBSerializer.fromBlob(rs.getBlob(1));
			}
			catch (SQLException sqe)
			{
				throw new ExecutionException(
					"SQL Exception in activity.", sqe);
			}
			finally
			{
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}

		@Override
		public File getCurrentWorkingDirectory()
		{
			return _cwd;
		}	
	}
}