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
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ggf.jsdl.Boundary_Type;
import org.ggf.jsdl.GPUArchitectureEnumeration;
import org.ggf.jsdl.GPUArchitecture_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.jsdl.LanciumEnvironment;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Resources_Type;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.container.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.resource.DBBESResource;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class BES
{
	static private Log _logger = LogFactory.getLog(BES.class);
	String _ipport="undefined";
	int _port;

	static private ServerDatabaseConnectionPool _connectionPool;

	static private ConcurrentHashMap<String, BES> _knownInstances = new ConcurrentHashMap<String, BES>();
	static private ConcurrentHashMap<String, BES> _activityToBESMap = new ConcurrentHashMap<String, BES>();

	static private String findBESEPI(Connection connection, String besid) throws SQLException
	{
		return BasicDBResource.getEPI(connection, besid);
	}

	static private void addActivityToBESMapping(String activityid, BES bes)
	{
		synchronized (_activityToBESMap) {
			_activityToBESMap.put(activityid, bes);
		}
	}

	static private void removeActivityToBESMapping(String activityid)
	{
		synchronized (_activityToBESMap) {
			_activityToBESMap.remove(activityid);
		}
	}

	static private void supplementCreationParamsWithTweakerConfig(String besid, Connection connection) throws SQLException, IOException
	{
		ConstructionParameters cParams = DBBESResource.constructionParameters(connection, GeniiBESServiceImpl.class, besid);

		if (CmdLineManipulatorUpgrader.upgrade((BESConstructionParameters) cParams))
			DBBESResource.constructionParameters(connection, besid, cParams);
	}

	static public Collection<String> listBESs()
	{
		synchronized (_knownInstances) {
			return new Vector<String>(_knownInstances.keySet());
		}
	}

	static public Collection<BESActivity> getAllActivities()
	{
		Collection<BESActivity> allActivities = new LinkedList<BESActivity>();

		synchronized (_knownInstances) {
			for (BES bes : _knownInstances.values()) {
				for (BESActivity activity : bes.getContainedActivities()) {
					allActivities.add(activity);
				}
			}
		}

		return allActivities;
	}

	static public BES findBESForActivity(String activityid)
	{
		synchronized (_activityToBESMap) {
			return _activityToBESMap.get(activityid);
		}
	}

	synchronized static public void loadAllInstances(ServerDatabaseConnectionPool connectionPool) throws SQLException, IOException
	{
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		if (connectionPool == null)
			throw new IllegalArgumentException("connectionPool argument cannot be null.");

		if (_connectionPool != null)
			throw new IllegalStateException("BES instances already loaded.");

		_connectionPool = connectionPool;

		try {
			connection = _connectionPool.acquire(false);
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT besid, userloggedinaction, screensaverinactiveaction, ipport " + "FROM bespolicytable");
			while (rs.next()) {
				String besid = rs.getString(1);
				String ipport =rs.getString(4);
				if (ipport ==null) ipport ="undefined";
				_logger.info(String.format("Loading BES with id: %s", besid));
				supplementCreationParamsWithTweakerConfig(besid, connection);

				BES bes = new BES(connection, besid, ipport);
				_knownInstances.put(besid, bes);

				// Load CloudMangaer if BES is a cloudBES
				BESConstructionParameters cParam =
						(BESConstructionParameters) DBBESResource.constructionParameters(connection, GeniiBESServiceImpl.class, besid);
				if (cParam.getCloudConfiguration() != null) {
					try {
						CloudMonitor.loadCloudInstance(besid, cParam.getCloudConfiguration());
					} catch (Exception e) {
						_logger.info("exception occurred in loadAllInstances", e);
					}
				}

				bes.reloadAllActivities(connection);

				connection.commit();
			}

		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized static public BES getBES(String besid) throws IllegalStateException
	{
		if (_connectionPool == null) {
			throw new IllegalStateException("BES instances not initialized.");
		}
		return _knownInstances.get(besid);
	}

	synchronized private void updatePort(String ipAddr) {
		// 2020-07-15 by ASG. Added to support BES/processwrapper communication.
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement("UPDATE bespolicytable SET ipport = ? " + "WHERE besid = ?");
			stmt.setString(1, ipAddr);

			stmt.setString(2, _besid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database.");
			connection.commit();
		} catch (SQLException e) {
			_logger.error("Unable to set BES IP Address/port.", e);
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}

	}

	synchronized static public BES createBES(String besid, ConstructionParameters params) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			if (_logger.isDebugEnabled())
				_logger.debug("Entering CreateBES");
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
					"INSERT INTO bespolicytable " + "(besid, userloggedinaction, screensaverinactiveaction, ipport) " + "VALUES (?, ?, ?, ?)");
			stmt.setString(1, besid);
			stmt.setString(2, "undefined");
			stmt.setString(3, "undefined");
			stmt.setString(4, "undefined");
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database table for bes creation.");
			connection.commit();
			if (_logger.isDebugEnabled())
				_logger.debug("Passed commit in CreateBES");

			BES bes;

			if (((BESConstructionParameters) params).getCloudConfiguration() != null) {
				try {
					CloudMonitor.loadCloudInstance(besid, ((BESConstructionParameters) params).getCloudConfiguration());
				} catch (Exception e) {
					_logger.error(e);
				}
			}

			if (_logger.isDebugEnabled())
				_logger.debug("Trying to put a new bes with besid: " + besid);
			_knownInstances.put(besid, bes = new BES(null, besid, "undefined"));
			return bes;
		} finally {
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}

	synchronized static public void deleteBES(Connection connection, String besid) throws SQLException
	{
		PreparedStatement stmt = null;

		BES bes = _knownInstances.get(besid);
		if (bes == null)
			throw new IllegalStateException("BES \"" + besid + "\" is unknown.");

		try {
			stmt = connection.prepareStatement("DELETE FROM bespolicytable WHERE besid = ?");
			stmt.setString(1, besid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database table for bes deletion.");

			_knownInstances.remove(besid);
			bes.delete(connection);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	private String _besid;
	private String _besEPI;

	private ConcurrentHashMap<String, BESActivity> _containedActivities = new ConcurrentHashMap<String, BESActivity>();
	private BESPWrapperConnection _comm;

	private BES(Connection connection, String besid, String ipport) throws SQLException
	{
		_besid = besid;
		if (_logger.isDebugEnabled())
			_logger.debug("Entering BES contructor");
		if (ipport.equals("undefined")) {
			// We need to fire up the listener with a null port
			// This sets the _ipaddr
			_comm = new BESPWrapperConnection(0, this);
			_ipport = _comm.getIPPort();
			updatePort(_ipport);
		}
		else {
			_ipport=ipport;	
			// ipaddr is of the form "xxx.eee.sss.eee:port" We need to grab the port
			String res[]=ipport.split(":");
			int port = Integer.parseInt(res[1]);
			_comm = new BESPWrapperConnection(port, this);
			// getIPAddr returns in form 192.168.0.0:5555, as an example
			_ipport = _comm.getIPPort();
		}
		if (_logger.isDebugEnabled()) {
			_logger.debug("BESPWrapperConnection: IPAddr/port is " + _ipport);
			_logger.debug("BESPWrapperConnection: starting listening...");
		}
		_comm.start();
		if (connection != null)
			_besEPI = findBESEPI(connection, besid);
		else
			_besEPI = null;
		if (_logger.isDebugEnabled())
			_logger.debug("Exiting BES contructor");
	}

	public String getBESID()
	{
		return _besid;
	}

	public String getBESipport() {
		return _ipport;
	}

	synchronized public boolean isAcceptingActivites(Integer threshold)
	{
		if (threshold != null)
			if (_containedActivities.size() >= threshold.intValue())
				return false;

		// Check if cloud with no available resources
		if (CloudMonitor.isCloudBES(getBESID())) {
			// get Manager
			CloudManager tManage = CloudMonitor.getManager(getBESID());
			if (tManage.available() <= 0)
				return false;
		}

		return true;
	}

	synchronized public Collection<BESActivity> getContainedActivities()
	{
		return new ArrayList<BESActivity>(_containedActivities.values());
	}

	synchronized private void delete(Connection connection) throws SQLException
	{
		for (String activity : _containedActivities.keySet()) {
			try {
				deleteActivity(connection, activity);
			} catch (UnknownActivityIdentifierFaultType uaift) {
				_logger.error("Unexpected exception thrown while deleting activity.", uaift);
			}
		}

		if (CloudMonitor.isCloudBES(getBESID())) {
			try {
				CloudMonitor.deleteCloudBES(getBESID());
			} catch (Exception e) {
				if (_logger.isDebugEnabled())
					_logger.debug(e);
			}
		}

	}

	synchronized private String findJobName(String suggestedJobName)
	{
		if (suggestedJobName == null) {
			SimpleDateFormat format = new SimpleDateFormat("ddMMMMyyyy.HHmm.ss");
			suggestedJobName = format.format(new Date());
		}

		int count = 0;

		Pattern pattern = Pattern.compile("^" + Pattern.quote(suggestedJobName) + " ([0-9]+)$");

		for (BESActivity activity : _containedActivities.values()) {
			String name = activity.getJobName();
			if (name.equals(suggestedJobName) && count == 0)
				count = 1;
			else {
				Matcher matcher = pattern.matcher(name);
				if (matcher.matches()) {
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

	synchronized public BESActivity createActivity(Connection parentConnection, String activityid, JobDefinition_Type jsdl,
		Collection<Identity> owners, ICallingContext callingContext, BESWorkingDirectory activityCWD, Vector<ExecutionPhase> executionPlan,
		EndpointReferenceType activityEPR, String activityServiceName, String suggestedJobName, String jobAnnotation, String gpuType, int gpuCount) throws SQLException, ResourceException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		_logger.info("Activity id: " + activityid);
		String jobName = findJobName(suggestedJobName);
		ActivityState state = new ActivityState(ActivityStateEnumeration.Pending, null);

		try {
			if (parentConnection != null)
				connection = parentConnection;
			else
				connection = _connectionPool.acquire(false);

			stmt = connection.prepareStatement("INSERT INTO besactivitiestable " + "(activityid, besid, jsdl, owners, callingcontext, "
					+ "state, submittime, " + "terminaterequested, activitycwd, executionplan, "
					+ "nextphase, activityepr, activityservicename, jobname, destroyrequested, ipport, persistrequested) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, activityid);
			stmt.setString(2, _besid);
			stmt.setBlob(3, DBSerializer.xmlToBlob(jsdl, "besactivitiestable", "jsdl"));
			stmt.setBlob(4, DBSerializer.toBlob(owners, "besactivitiestable", "owners"));
			stmt.setBlob(5, DBSerializer.toBlob(callingContext, "besactivitiestable", "callingcontext"));
			stmt.setBlob(6, DBSerializer.toBlob(state, "besactivitiestable", "state"));
			stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
			stmt.setShort(8, (short) 0);
			stmt.setString(9,
					String.format("%s|%s", activityCWD.mustDelete() ? "d" : "k", activityCWD.getWorkingDirectory().getAbsolutePath()));
			stmt.setBlob(10, DBSerializer.toBlob(executionPlan, "besactivitiestable", "executionplan"));
			stmt.setInt(11, 0);
			stmt.setBlob(12, EPRUtils.toBlob(activityEPR, "besactivitiestable", "activityepr"));
			stmt.setString(13, activityServiceName);
			stmt.setString(14, jobName);
			//LAK 2020 Aug 28: Default destroyrequested to false
			stmt.setShort(15, (short)0);
			stmt.setString(16, _ipport);
			//LAK 2020 Dec 15: Default persistrequested to false
			stmt.setShort(17, (short)0);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update database for bes activity creation.");
			connection.commit();
			
			String lanciumEnvironment = null;
			JobDefinition_Type jobDef = jsdl;
			JobDescription_Type jobDesc = jobDef.getJobDescription(0);
			if (jobDesc != null) {
				Resources_Type resources = jobDesc.getResources();
				if (resources != null) {
					LanciumEnvironment[] lanciumEnvArray = resources.getLanciumEnvironment();
					if (lanciumEnvArray != null) {
						LanciumEnvironment lanciumEnv = lanciumEnvArray[0];
						if (lanciumEnv != null) {
							lanciumEnvironment = lanciumEnv.toString();
						}
					}
				}
			}
			BESActivity activity = new BESActivity(_connectionPool, this, activityid, state, activityCWD, executionPlan, 0,
				activityServiceName, jobName, jobAnnotation, gpuType, gpuCount, false, false, false, lanciumEnvironment, _ipport);
			_containedActivities.put(activityid, activity);
			addActivityToBESMapping(activityid, this);
			return activity;
		} finally {
			StreamUtils.close(stmt);

			if (parentConnection == null)
				_connectionPool.release(connection);
		}
	}

	synchronized public BESActivity findActivity(String activityid)
	{
		return _containedActivities.get(activityid);
	}

	synchronized public void deleteActivity(Connection connection, String activityid) throws UnknownActivityIdentifierFaultType, SQLException
	{
		UnknownActivityIdentifierFaultType fault = null;
		if (_logger.isDebugEnabled())
			_logger.debug("BES: deleteActivity " + activityid);
		BESActivity activity = _containedActivities.get(activityid);

		if (activity == null)
			fault = new UnknownActivityIdentifierFaultType("Couldn't find activity \"" + activityid + "\".", null);
		else
			StreamUtils.close(activity);

		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("DELETE FROM besactivitiestable WHERE activityid = ?");
			stmt.setString(1, activityid);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			stmt = connection.prepareStatement("DELETE FROM besactivitypropertiestable WHERE activityid = ?");
			stmt.setString(1, activityid);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			stmt = connection.prepareStatement("DELETE FROM besactivityfaultstable WHERE besactivityid = ?");
			stmt.setString(1, activityid);
			stmt.executeUpdate();

			_containedActivities.remove(activityid);
			removeActivityToBESMapping(activityid);
			
			if (fault != null)
				throw fault;
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@SuppressWarnings("unchecked")
	synchronized private void reloadAllActivities(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection
				.prepareStatement("SELECT activityid, state, " + "terminaterequested, activitycwd, executionplan, "
					+ "nextphase, activityservicename, jobname, jsdl, destroyrequested, ipport, persistrequested" + "FROM besactivitiestable WHERE besid = ?");

			int count = 0;
			stmt.setString(1, _besid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String activityid = null;
				try {
					activityid = rs.getString(1);
					ActivityState state = (ActivityState) DBSerializer.fromBlob(rs.getBlob(2));
					boolean terminateRequested = (rs.getShort(3) == 0) ? false : true;
					//LAK 2020 Aug 28: Read in destroyrequested flag from the DB
					boolean destroyRequested = (rs.getShort(10) == 0) ? false : true;
					//LAK 2020 Dec 15: Read in persistrequested flag from DB
					boolean persistRequested = (rs.getShort(12) == 0) ? false : true;

					String activityCWDString = rs.getString(4);
					BESWorkingDirectory activityCWD;

					if (activityCWDString.startsWith("d|"))
						activityCWD = new BESWorkingDirectory(new File(activityCWDString.substring(2)), true);
					else
						activityCWD = new BESWorkingDirectory(new File(activityCWDString.substring(2)), false);

					Vector<ExecutionPhase> executionPlan = (Vector<ExecutionPhase>) DBSerializer.fromBlob(rs.getBlob(5));
					int nextPhase = rs.getInt(6);
					String activityServiceName = rs.getString(7);
					String jobName = rs.getString(8);
					
					// 2020 August 5 by CCH - Part of BES dataflow cleanup`
					// The plan: Put everything (jobName, jobAnnotation, gpuCount + Type, etc) into BESActivity and pass reference to activity to each phase
					// Because jobAnnotation is now being stored in the BESActivity rather than in the phases we need to reload it from the JSDL
					// Previously, we wouldn't need to reload it because it would already be in the phases inside the executionPlan
					String jobAnnotation = null;
					String gpuType = null;
					String lanciumEnvironment = null;
					int gpuCount = 0;
					JobDefinition_Type jsdl = DBSerializer.xmlFromBlob(JobDefinition_Type.class, rs.getBlob(9));
					if (jsdl !=null) {
						JobDescription_Type jobDesc = jsdl.getJobDescription(0);
						if (jobDesc != null) {
							JobIdentification_Type jobID=jobDesc.getJobIdentification();
							if (jobID!=null) {
								String []annotations=jobID.getJobAnnotation();
								if (annotations!=null) {
									jobAnnotation=jobID.getJobAnnotation(0);
								}
							}
							Resources_Type resources = jobDesc.getResources();
							if (resources != null) {
								GPUArchitecture_Type gpuArch = resources.getGPUArchitecture();
								if (gpuArch != null) {
									GPUArchitectureEnumeration gpuArchName = gpuArch.getGPUArchitectureName();
									if (gpuArchName != null) {
										gpuType = gpuArchName.getValue();
									}
								}
								LanciumEnvironment[] lanciumEnvArray = resources.getLanciumEnvironment();
								if (lanciumEnvArray != null) {
									LanciumEnvironment lanciumEnv = lanciumEnvArray[0];
									if (lanciumEnv != null) {
										lanciumEnvironment = lanciumEnv.toString();
									}
								}
								RangeValue_Type gpuCountPerNode = resources.getGPUCountPerNode();
								if (gpuCountPerNode != null) {
									Boundary_Type upperBound = gpuCountPerNode.getUpperBoundedRange();
									if (upperBound != null) {
										gpuCount = (int) upperBound.get_value();
									}
								}
							}
						}
					}
					if (_logger.isDebugEnabled()) {
						_logger.debug("Job annotation: " + jobAnnotation +". For job " + activityid +".");
						_logger.debug("GPU type: " + gpuType +". For job " + activityid +".");
						_logger.debug("GPU count: " + gpuCount +". For job " + activityid +".");
					}
					String ipport = rs.getString(11);

					_logger.info(String.format("Starting activity %d\n", count++));					
					
					BESActivity activity = new BESActivity(_connectionPool, this, activityid, state, activityCWD, executionPlan, nextPhase,
							activityServiceName, jobName, jobAnnotation, gpuType, gpuCount, terminateRequested, persistRequested, destroyRequested, 
								lanciumEnvironment, ipport);
					_containedActivities.put(activityid, activity);

					addActivityToBESMapping(activityid, this);
				} catch (Throwable cause) {
					_logger.error("Error loading activity from database.", cause);
				}
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	synchronized public String getBESEPI()
	{
		if (_besEPI == null) {
			Connection conn = null;

			try {
				conn = _connectionPool.acquire(true);
				_besEPI = findBESEPI(conn, _besid);
			} catch (SQLException cause) {
				throw new RuntimeException("Unable to find BES EPI.", cause);
			} finally {
				_connectionPool.release(conn);
			}
		}

		return _besEPI;
	}

	public BESPWrapperConnection getBESPWrapperConnection() {
		return _comm;
	}
	public boolean sendCommand(String activityid, String command) {
		if (_containedActivities.containsKey(activityid)) {
			return _containedActivities.get(activityid).sendCommand(command);
		} else {
			_logger.info("BES sendCommand call: BES does not have BESActivity with activityid: " + activityid);
			return false;
		}
	}
}
