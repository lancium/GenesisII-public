package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
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
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.bes.ExecutionException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.QueueResultsException;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityStateChangedContents;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.AbstractRunProcessPhase;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class BESActivity implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESActivity.class);

	private ServerDatabaseConnectionPool _connectionPool;

	private boolean _finishCaseHandled = false;
	private BES _bes;
	private String _activityid;
	private ActivityState _state;
	private boolean _terminateRequested;
	public boolean _persistRequested;
	private boolean _destroyRequested;
	private BESWorkingDirectory _activityCWD;
	private Vector<ExecutionPhase> _executionPlan;
	private int _nextPhase;
	private String _activityServiceName;
	private String _jobName;
	private ActivityRunner _runner;
	private String _IPPort;
	private String _jobAnnotation;
	private String _gpuType;
	private int _gpuCount;
	//LAK 2020 Aug 18: This is set to true when the execution environment is fully setup before the phase is executed
	private boolean _executionContextSet = false;
	private String _lanciumEnvironment;


	public BESActivity(ServerDatabaseConnectionPool connectionPool, BES bes, String activityid, ActivityState state,
		BESWorkingDirectory activityCWD, Vector<ExecutionPhase> executionPlan, int nextPhase, String activityServiceName, String jobName, String jobAnnotation,
		String gpuType, int gpuCount, boolean terminateRequested, boolean destroyRequested, boolean persistRequested, 
		String lanciumEnvironment, String IPPort)
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
		_IPPort=IPPort;

		_terminateRequested = terminateRequested;
		_persistRequested = persistRequested;
		_destroyRequested = destroyRequested;
		_jobAnnotation = jobAnnotation;
		_gpuType = gpuType;
		_gpuCount = gpuCount;
		_lanciumEnvironment = lanciumEnvironment;

		_runner = new ActivityRunner(_terminateRequested, _destroyRequested);
		
		startRunner();
	}
	
	public void startRunner() {
		if (!handleFinishedCase()) {
			Thread thread = new Thread(_runner, "BES Activity Runner Thread");
			thread.setDaemon(true);
			thread.start();
		}
	}

	public boolean isGood() throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT callingcontext " + "FROM besactivitiestable " + "WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				return false;

			ICallingContext cctxt = (ICallingContext) DBSerializer.fromBlob(rs.getBlob(1));

			return ContextManager.isGood(cctxt);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	public String getActivityID()
	{
		return _activityid;
	}

	public File getAccountingDir() 
	{
		File f=getActivityCWD().getWorkingDirectory();
		String JWD=f.getName();
		String sharedDir=f.getParent();
		File accountingDirectory= new File(sharedDir+"/Accounting/"+JWD);
		return accountingDirectory;
	}
	
	public BESWorkingDirectory getActivityCWD()
	{
		return _activityCWD;
	}

	@SuppressWarnings("unchecked")
	synchronized public void verifyOwner() throws GenesisIISecurityException, SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT owners FROM besactivitiestable " + "WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Unable to load owner information from database " + "for bes activity.");

			if (!QueueSecurity.isOwner((Collection<Identity>) DBSerializer.fromBlob(rs.getBlob(1))))
				throw new GenesisIISecurityException(
					"Caller does not have permission to get " + "activity status for activity \"" + _activityid + "\".");
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized public EndpointReferenceType getActivityEPR() throws SQLException, ResourceException, NoSuchActivityFault
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT activityepr FROM besactivitiestable " + "WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new NoSuchActivityFault(_activityid);
			return EPRUtils.fromBlob(rs.getBlob(1));
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	// LAK 2020 Sept 17: Removed synchronized keyword from this function. This caused deadlocks and is not needed.
	// See the stack trace uploaded to https://drive.google.com/file/d/16uKyFUsv42p_a3LhmA9FWKisujLtC-2T/view?usp=sharing for
	// more information about WHY this caused issues.
	public JobDefinition_Type getJobDefinition() throws SQLException, IOException, ClassNotFoundException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT jsdl FROM besactivitiestable " + "WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Unable to find activity in database.");
			return DBSerializer.xmlFromBlob(JobDefinition_Type.class, rs.getBlob(1));
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized public Collection<Throwable> getFaults() throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Collection<Throwable> faults = new LinkedList<Throwable>();

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT fault FROM besactivityfaultstable " + "WHERE besactivityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				faults.add((Throwable) DBSerializer.fromBlob(rs.getBlob(1)));
			}

			return faults;
		} finally {
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
		try {
			terminate();
		} catch (Throwable ee) {
			_logger.error("Problem trying to early terminate activity.", ee);
		}
	}

	public String getJobName()
	{
		return _jobName;
	}
	
	public String getIPPort() {
		return _IPPort;
	}
	
	public boolean sendCommand(String commandToSend) {
		// commandToSend should also contain activityid
		_logger.info("SendCommand called with command: " + commandToSend);
		// _IPPort must be in the form <ip>:<port>
		if (_IPPort == null || _IPPort.equals("undefined") || !_IPPort.contains(":")) return false;
		String[] ipport = _IPPort.split(":");
		String ipaddr = ipport[0];
		int port = Integer.parseInt(ipport[1]);
		Socket socket = null;
		try {
			_logger.info("Attempting to connect to: " + ipaddr + ":" + port);
			socket = new Socket(ipaddr, port);
			_logger.info("Connected to: " + ipaddr + ":" + port);
		} catch (IOException e) {
			_logger.error("Unable to set up socket connection with " + ipaddr + ":" + port + ".", e);
			return false;
		} catch (Exception e) {
			_logger.error("Caught unknown exception while trying to set up socket connection with " + ipaddr + ":" + port + ".", e);
			return false;
		}
		boolean success = false;
		try
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
			output.println(commandToSend);
			String response = input.readLine();
			_logger.info("Received message from: " +  _IPPort + ". Msg: " + response);
			success = response == null ? false : response.equals(_activityid + " OK");
			_logger.info("Success? " + success);
			socket.close();
		}
		catch (IOException e) {
			_logger.error("Lost connection to socket while handing " + commandToSend, e);
		}
		return success;
	}
	
	public String getJobAnnotation()
	{
		return _jobAnnotation;
	}

	synchronized public void terminate() throws ExecutionException, SQLException
	{
		if (_terminateRequested)
			return;
		
		updateState(true, _destroyRequested, _persistRequested);
		if (_runner != null)
			_runner.requestTerminate(false);
	}

	synchronized public void persist() throws ExecutionException, SQLException
	{
		if (_persistRequested)
			return;

		updateState(_terminateRequested, _destroyRequested, true);
		updateState(new ActivityState(ActivityStateEnumeration.Persisted, null));
		if (_runner != null)
			_runner.setExecutionToPersisted();
	}
	
	synchronized public void restart() throws ExecutionException, SQLException
	{
		if (!_persistRequested)
			return;

		// TODO: Handle restart state
		updateState(_terminateRequested, _destroyRequested, false);
		updateState(new ActivityState(ActivityStateEnumeration.Running, null));
		if (_runner != null)
			_runner.setExecutionToRestart();
	}
	
	//LAK: Freeze/thaw is a little special in that the BESActivity doesn't do anything, this is just to alert to Activity
	// to the new state
	synchronized public void freeze() throws ExecutionException, SQLException
	{
		updateState(new ActivityState(ActivityStateEnumeration.Frozen, null));
	}
	
	//LAK: Freeze/thaw is a little special in that the BESActivity doesn't do anything, this is just to alert to Activity
	// to the new state
	synchronized public void thaw() throws ExecutionException, SQLException
	{
		updateState(new ActivityState(ActivityStateEnumeration.Running, null));
	}
	
	synchronized public void stopExecutionThread() throws ExecutionException, SQLException
	{
		if(_runner != null)
			_runner.requestDestruction();
	}
	
	synchronized public ActivityState getState()
	{
		ActivityState retState = (ActivityState) _state.clone();

		return retState;
	}

	synchronized private void updateState(boolean terminateRequested, boolean destroyRequested, boolean persistRequested) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = _connectionPool.acquire(true);
			//LAK: 2020 Aug 27: Added destroyrequested to the DB
			stmt = connection.prepareStatement(
				"UPDATE besactivitiestable " + "SET terminaterequested = ?, destroyrequested = ?, persistrequested = ?" + "WHERE activityid = ?");
			stmt.setShort(1, terminateRequested ? (short) 1 : (short) 0);
			stmt.setShort(2, destroyRequested ? (short) 1 : (short) 0);
			stmt.setShort(3, persistRequested ? (short) 1 : (short) 0);
			stmt.setString(4, _activityid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database.");
			connection.commit();

			_terminateRequested = terminateRequested;
			_destroyRequested = destroyRequested;
			_persistRequested = persistRequested;
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized private void updateState(ActivityState state) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("UPDATE besactivitiestable SET state = ? " + "WHERE activityid = ?");
			stmt.setBlob(1, DBSerializer.toBlob(state, "besactivitiestable", "state"));
			stmt.setString(2, _activityid);
			try {
				stmt.executeUpdate();
			} catch (SQLException sqe) {
				_logger.error("Unable to update state of besactivitiestable.", sqe);
				throw new SQLException("Unable to update database.");
			}
			connection.commit();

			_state = state;

			notifyStateChange();
		} catch (ResourceException e) {
			_logger.warn("Unable to send notification for status change.", e);
		} catch (ResourceUnknownFaultType e) {
			_logger.warn("Unable to send notification for status change.", e);
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	public synchronized void updateIPPort(String IPPort) throws SQLException {
		{
			Connection connection = null;
			PreparedStatement stmt = null;

			try {
				connection = _connectionPool.acquire(true);
				stmt = connection.prepareStatement("UPDATE besactivitiestable SET ipport = ? " + "WHERE activityid = ?");
				stmt.setString(1, IPPort);
				stmt.setString(2, _activityid);
				if (stmt.executeUpdate() != 1)
					throw new SQLException("Unable to update database.");
				connection.commit();

				_IPPort=IPPort;
			} finally {
				StreamUtils.close(stmt);
				_connectionPool.release(connection);
			}
		}	
	}
	
	synchronized private void updateState(int nextPhase, ActivityState state) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("UPDATE besactivitiestable SET nextphase = ?, state = ? " + "WHERE activityid = ?");
			stmt.setInt(1, nextPhase);
			stmt.setBlob(2, DBSerializer.toBlob(state, "besactivitiestable", "state"));
			stmt.setString(3, _activityid);
			try {
				stmt.executeUpdate();
			} catch (SQLException sqe) {
				_logger.error("Unable to update state of besactivitiestable.", sqe);
				throw new SQLException("Unable to update database.");
			}
			connection.commit();

			_nextPhase = nextPhase;
			_state = state;

			notifyStateChange();
		} catch (ResourceException e) {
			_logger.warn("Unable to send notification for status change.", e);
		} catch (ResourceUnknownFaultType e) {
			_logger.warn("Unable to send notification for status change.", e);
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	private WorkingContext createWorkingContext() throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("SELECT callingcontext, activityepr " + "FROM besactivitiestable " + "WHERE activityid = ?");
			stmt.setString(1, _activityid);
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException("Activity \"" + _activityid + "\" does not exist.");
			ICallingContext cctxt = (ICallingContext) DBSerializer.fromBlob(rs.getBlob(1));
			EndpointReferenceType epr = EPRUtils.fromBlob(rs.getBlob(2));

			WorkingContext ret = new WorkingContext();
			ret.setProperty(WorkingContext.EPR_PROPERTY_NAME, epr);
			ret.setProperty(WorkingContext.TARGETED_SERVICE_NAME, _activityServiceName);
			ret.setProperty(WorkingContext.CURRENT_CONTEXT_KEY, cctxt);
			return ret;
		} catch (ResourceException re) {
			throw new SQLException("Unable to load working context.", re);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	synchronized public void notifiyPwrapperIsTerminating() {
		if(_logger.isDebugEnabled())
			_logger.debug("BESActivity has been notified by pwrapper that it is terminating");
		_runner.notifiyPwrapperIsTerminating();
	}
	
	synchronized public void notifyPersistedSuceeded() throws SQLException {
		updateState(new ActivityState(ActivityStateEnumeration.Persisted, null));
	}

	private void execute(ExecutionPhase phase) throws Throwable
	{
		WorkingContext ctxt = createWorkingContext();

		try {
			ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY,
				new ResourceKey(_activityServiceName, new AddressingParameters(_activityid, null, null)));
			WorkingContext.setCurrentWorkingContext(ctxt);
			_executionContextSet = true;
			phase.execute(getExecutionContext(), this);
		} finally {
			_executionContextSet = false;
			WorkingContext.setCurrentWorkingContext(null);
		}
	}

	private void notifyStateChange() throws ResourceException, ResourceUnknownFaultType, SQLException
	{
		WorkingContext ctxt = createWorkingContext();
		ActivityState state = getState();

		try {
			WorkingContext.setCurrentWorkingContext(ctxt);
			TopicSet space = TopicSet.forPublisher(BESActivityServiceImpl.class);
			TopicPath topicPath;

			if (state.isFinalState())
				topicPath = BESActivityServiceImpl.ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC;
			else
				topicPath = BESActivityServiceImpl.ACTIVITY_STATE_CHANGED_TOPIC;

			PublisherTopic topic = space.createPublisherTopic(topicPath);
			topic.publish(new BESActivityStateChangedContents(state));
		} finally {
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
				_state = (ActivityState) newState.clone();
			}

			@Override
			public ICallingContext getCallingContext() throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;

				try {
					connection = _connectionPool.acquire(true);
					stmt = connection.prepareStatement("SELECT callingcontext FROM besactivitiestable " + "WHERE activityid = ?");
					stmt.setString(1, _activityid);
					rs = stmt.executeQuery();
					if (!rs.next())
						throw new SQLException("Unable to find activity in database.");
					return (ICallingContext) DBSerializer.fromBlob(rs.getBlob(1));
				} catch (SQLException sqe) {
					throw new ExecutionException("Database error trying to get calling context.", sqe);
				} finally {
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
			public BESWorkingDirectory getCurrentWorkingDirectory() throws ExecutionException
			{
				return _activityCWD;
			}

			@Override
			public Serializable getProperty(String name) throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;
				ResultSet rs = null;

				try {
					connection = _connectionPool.acquire(true);
					stmt = connection.prepareStatement(
						"SELECT propertyvalue FROM besactivitypropertiestable " + "WHERE activityid = ? AND propertyname = ?");
					stmt.setString(1, _activityid);
					stmt.setString(2, name);

					rs = stmt.executeQuery();
					if (!rs.next())
						return null;

					return (Serializable) DBSerializer.fromBlob(rs.getBlob(1));
				} catch (SQLException sqe) {
					throw new ExecutionException("Database error trying to get activity property.", sqe);
				} finally {
					StreamUtils.close(rs);
					StreamUtils.close(stmt);
					_connectionPool.release(connection);
				}
			}

			@Override
			public void setProperty(String name, Serializable value) throws ExecutionException
			{
				Connection connection = null;
				PreparedStatement stmt = null;

				try {
					connection = _connectionPool.acquire(false);
					stmt =
						connection.prepareStatement("DELETE FROM besactivitypropertiestable " + "WHERE activityid = ? AND propertyname = ?");
					stmt.setString(1, _activityid);
					stmt.setString(2, name);
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
					if (value != null) {
						stmt = connection.prepareStatement("INSERT INTO besactivitypropertiestable " + "VALUES (?, ?, ?)");

						stmt.setString(1, _activityid);
						stmt.setString(2, name);
						stmt.setBlob(3, DBSerializer.toBlob(value, "besactivitypropertiestable", "propertyvalue"));
						stmt.executeUpdate();
					}

					connection.commit();
				} catch (SQLException sqe) {
					throw new ExecutionException("Database error trying to get activity property.", sqe);
				} finally {
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

		try {
			connection = _connectionPool.acquire(true);

			try {
				blob = DBSerializer.toBlob(cause, "besactivityfaultstable", "fault");
			} catch (SQLException sqe) {
				_logger.error("Attempt to serialize an unserializable " + "exception into the database.", cause);
				addFault(new Exception("Unserializable fault occurred in BES activity (" + cause.getLocalizedMessage()
					+ ") -- no further information available."), attemptsLeft - 1);
				return;
			}

			stmt = connection.prepareStatement("INSERT INTO besactivityfaultstable " + "(besactivityid, fault) " + "VALUES(?, ?)");
			stmt.setString(1, _activityid);
			stmt.setBlob(2, blob);
			stmt.executeUpdate();
			connection.commit();
		} catch (Throwable cause2) {
			_logger.error("Unexpected error while adding fault " + "to bes activity fault database.", cause2);
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	private void cleanupUnnecessaryMemory()
	{
		// Now that we are done running, we should free up any memory that
		// we no longer need to use.

		/*
		 * These either can't be free'd (they aren't objects), or they are merely references to objects that are held in other places:
		 * _connectionPool; _terminateRequested; _nextPhase; _bes
		 */


		_executionPlan.clear();
		_executionPlan = null;
		_runner = null;
		_activityServiceName = null;

		/*
		 * And these we may need later: _activityid _state _jobName; _activityCWD = null;
		 */
	}

	final private boolean finishedExecution()
	{
		return _nextPhase >= _executionPlan.size();
	}

	private boolean containsIgnoreableFault(Collection<Throwable> faults)
	{
		for (Throwable fault : faults) {
			if (fault instanceof IgnoreableFault)
				return true;
		}

		return false;
	}

	private boolean handleFinishedCase()
	{
		if (finishedExecution()) {
			synchronized (this) {
				if (_finishCaseHandled)
					return true;
				_finishCaseHandled = true;
			}

			try {
				Collection<Throwable> faults = getFaults();
				if (getFaults().size() > 0) {
					if (!containsIgnoreableFault(faults)) {
						updateState(_executionPlan.size(), new ActivityState(ActivityStateEnumeration.Failed, null));
					} else {
						updateState(_executionPlan.size(), new ActivityState(ActivityStateEnumeration.Failed, "Ignoreable"));
					}
				} else
					updateState(_executionPlan.size(), new ActivityState(ActivityStateEnumeration.Finished, null));
			} catch (SQLException cause) {
				_logger.error("BES Activity Unrecoverably Faulted.", cause);
				addFault(cause, 3);
				try {
					updateState(_executionPlan.size(), new ActivityState(ActivityStateEnumeration.Failed, null));
				} catch (Throwable cause2) {
					_logger.error("Unexpected exception occured in bes activity.", cause2);
					return true;
				}
			} finally {
				cleanupUnnecessaryMemory();
			}

			return true;
		} else
			return false;
	}

	public String getGPUType() {
		return _gpuType;
	}

	public void setGPUType(String gpuType) {
		this._gpuType = gpuType;
	}

	public int getGPUCount() {
		return _gpuCount;
	}

	public void setGPUCount(int gpuCount) {
		this._gpuCount = gpuCount;
	}
	
	public String getLanciumEnvironment() {
		return _lanciumEnvironment;
	}

	private class ActivityRunner implements Runnable
	{
		private boolean _terminateRequested = false;
		private boolean _destroyRequested = false;

		private Object _phaseLock;
		private ExecutionPhase _currentPhase = null;

		public ActivityRunner(boolean terminateRequested, boolean destroyRequested)
		{
			_phaseLock = BESActivity.this;
			_terminateRequested = terminateRequested;
			_destroyRequested = destroyRequested;
		}


		//LAK 2020 Aug 18: This method will handle creating a working context for the terminate call if execute() has not already ran (and done so).
		private boolean setupWorkingContextForTerminate()
		{
			if (_executionContextSet)
			{
				return false;
			}
			else
			{
				_logger.debug("Having to generate a working context before calling terminate.");
				try {
					WorkingContext ctxt = createWorkingContext();
					ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY,
							new ResourceKey(_activityServiceName, new AddressingParameters(_activityid, null, null)));
					WorkingContext.setCurrentWorkingContext(ctxt);
				} catch (SQLException | ResourceUnknownFaultType | ResourceException e1) {
					_logger.error("Error while creating a working context in BESActivity:terminate.");
				}
				return true;
			}
		}

		public void requestTerminate(boolean countAsFailedAttempt) throws ExecutionException
		{
			synchronized (_phaseLock) {
				if (_terminateRequested)
					return;

				_terminateRequested = true;
				_destroyRequested = false;

				//LAK: 2020 Aug 13: We have to call this to interrupt any terminateable phase that is currently running.
				if (_currentPhase != null && _currentPhase instanceof TerminateableExecutionPhase) {
					boolean selfManagedContext = false;
					try
					{
						//if setupWorkingContextForTerminate returns true, then this means that we have to also clear the created context
						selfManagedContext = setupWorkingContextForTerminate();
						((TerminateableExecutionPhase) _currentPhase).terminate(countAsFailedAttempt);
					}
					finally
					{
						if (selfManagedContext)
							WorkingContext.setCurrentWorkingContext(null);
					}
				}
				else
				{
					_phaseLock.notify();
				}
			}
		}
		
		// 2020 August 20 by CCH
		// requestPersist sets a boolean, _persisted to true
		// During the execution loop, if _persisted is true, we won't proceed to the next phase.
		// 2021 Jan 26 LAK
		// changed to setExecutionToPersisted to reflect that this is not handling anything outside of execution context
		public void setExecutionToPersisted() throws ExecutionException
		{
			synchronized (_phaseLock) {
				if (_persistRequested)
					return;

				_persistRequested = true;
				_terminateRequested = true;
			}
		}
		
		//LAK: WIP, does not work. Meant to handle restarting a persisted job.
		public void setExecutionToRestart() throws ExecutionException
		{
			synchronized (_phaseLock) {
				if (!_persistRequested)
					return;

				_persistRequested = false;
				_terminateRequested = false;
				
				ExecutionPhase currentPhase = _runner._currentPhase;
				
				if (currentPhase instanceof AbstractRunProcessPhase) {
					_executionPlan.insertElementAt(currentPhase, _nextPhase);
				}
				
				startRunner();
			}
		}
		
		//LAK: 31 Dec 2020 This handles informing the execution environment that the pwrapper is terminating
		public void notifiyPwrapperIsTerminating()
		{
			synchronized(_phaseLock)
			{
				if(_runner._currentPhase instanceof AbstractRunProcessPhase)
				{
					AbstractRunProcessPhase phase = (AbstractRunProcessPhase)_runner._currentPhase;
					phase.notifyPwrapperIsTerminating();
				}
			}
		}
		
		//LAK 2020 Aug 17: Since we removed _terminateRequested causing this thread to exit.
		//We had to add a new flag to have it exit immediately when job short-circuiting is required. 
		//Currently this is only used for requeuing jobs since we don't continue with the normal job cycle
		//and instead immediately destroy the job. This should replicate the previous behavior seen when terminate
		//was called in the past. 
		public void requestDestruction() throws ExecutionException
		{
			synchronized (_phaseLock)
			{
				_terminateRequested = false;
				_destroyRequested = true;
			}
		}

		public void run()
		{
			while (true) {
				try {
					synchronized (_phaseLock) {
						if (handleFinishedCase())
							break;

						if (_terminateRequested) {
							// Ensure Cloud Resources Cleaned up
							CloudMonitor.freeActivity(_activityid, _bes.getBESID());
						}

						_currentPhase = _executionPlan.get(_nextPhase);
						_logger.debug("BES Activity transitition to " + _currentPhase.getPhaseState().toString());
						updateState(_nextPhase, _currentPhase.getPhaseState());
						
						_logger.debug("checking terminate requested flag before executing plan=" + _currentPhase.getPhaseState() + " with _terminateRequested=" + _terminateRequested);
						if (_terminateRequested)
						{
							if (_currentPhase != null) {
								_logger.debug("about to check if currentPhase=" + _currentPhase.getPhaseState() + " is a TerminateableExecutionPhase");
								//LAK: we ONLY want to skip the phases that are marked as a TerminateableExecutionPhase
								//CCH: Only skip this phase if we don't want to persist
								if (_currentPhase instanceof TerminateableExecutionPhase) {
									//check if there is a valid current working context
									boolean selfManagedContext = false;
									try
									{
										selfManagedContext = setupWorkingContextForTerminate();
										((TerminateableExecutionPhase) _currentPhase).terminate(false);
									}
									finally
									{
										if (selfManagedContext)
										{
											WorkingContext.setCurrentWorkingContext(null);
										}
									}
									
									//LAK: Now we want to just skip to the next phase
									if (!_persistRequested) {
										_currentPhase = null;
										updateState(_nextPhase + 1, _state);
										continue;
									}
								}
							}
						}
					}
					
					synchronized(_phaseLock)
					{
						// 2020 August 20 by CCH
						// if we want to persist, we need to stop phase execution.
						if(_destroyRequested || _persistRequested) {
							//updateState(new ActivityState(ActivityStateEnumeration.Persisted, null));
							return;
						}
					}
					
					try {
						execute(_currentPhase);
					} catch (QueueResultsException qre) {
						// Ok, the job terminated without a queue results file being generated, therefore the pwrapper did not complete.
						// So, basically the system lost the job, the queue system killed it, the node died, something like that
						// We want to move to the next phase. But do we want it to count against tries?		
						_logger.debug("BES Activity faulted with QueueResultsException - continuing as planned.", qre);
						addFault(qre, 3);
					} catch (ContinuableExecutionException cee) {
						addFault(cee, 3);
					} catch (InterruptedException ie) {
						Thread.currentThread().isInterrupted();
					}

					synchronized (_phaseLock) {
						// 2020 August 20 by CCH
						// if we want to persist, we let the job end execution.
						if(_destroyRequested || _persistRequested)
							return;
						_currentPhase = null;
						updateState(_nextPhase + 1, _state);
					}
				} catch (Throwable cause) {

					_logger.error("BES Activity Unrecoverably Faulted.", cause);
					addFault(cause, 3);
					try {
						// Ensure Cloud Resource cleaned up
						CloudMonitor.freeActivity(_activityid, _bes.getBESID());

						updateState(_executionPlan.size(), new ActivityState(ActivityStateEnumeration.Failed, null));
					} catch (Throwable cause2) {
						_logger.error("Unexpected exception occured in bes activity.", cause2);
						return;
					}
				}
			}
		}
	}
}
