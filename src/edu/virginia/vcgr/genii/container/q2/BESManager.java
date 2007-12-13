package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.rns.EntryType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class BESManager implements Closeable
{
	static private final long _BES_UPDATE_CYCLE = 1000L * 60 * 5;
	static private final int _MISS_CAP = 10;
	
	static private Log _logger = LogFactory.getLog(BESManager.class);
	
	volatile private boolean _closed = false;
	
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	private DatabaseConnectionPool _connectionPool;
	
	private HashMap<Long, BESData> _containersByID = 
		new HashMap<Long, BESData>();
	private HashMap<String, BESData> _containersByName = 
		new HashMap<String, BESData>();
	private HashMap<Long, BESUpdateInformation> _updateInformation = 
		new HashMap<Long, BESUpdateInformation>();
			// Being on this list doesn't mean you have slots available, all it means is that you are responsive
	private HashMap<Long, BESData> _scheduleableContainers = 
		new HashMap<Long, BESData>();
	private ThreadPool _outcallThreadPool;
	
	private BESResourceUpdater _updater;
	
	public BESManager(ThreadPool outcallThreadPool, QueueDatabase database, SchedulingEvent schedulingEvent,
		Connection connection, DatabaseConnectionPool connectionPool) 
		throws SQLException, ResourceException, 
			ConfigurationException, GenesisIISecurityException
	{
		_connectionPool = connectionPool;
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		
		loadFromDatabase(connection);
		
		_updater = new BESResourceUpdater(connectionPool, this, _BES_UPDATE_CYCLE / 10);
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		_updater.close();
	}
	
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException, ResourceException, 
			ConfigurationException, GenesisIISecurityException
	{
		Collection<BESData> allBESs = _database.loadAllBESs(connection);
		
		for (BESData bes : allBESs)
		{
			_containersByID.put(new Long(bes.getID()), bes);
			_containersByName.put(bes.getName(), bes);
			_updateInformation.put(new Long(bes.getID()), 
				new BESUpdateInformation(bes.getID(), _BES_UPDATE_CYCLE, _MISS_CAP));
		}
		
		updateResources(connection);
	}
	
	synchronized public void addNewBES(Connection connection, String name,
			EndpointReferenceType epr)
		throws SQLException, ResourceException, ConfigurationException,
			GenesisIISecurityException
	{
		BESUpdateInformation updateInfo;
		Collection<BESUpdateInformation> toUpdate = 
			new ArrayList<BESUpdateInformation>(1);
		
		long id = _database.addNewBES(connection, name, epr);
		connection.commit();
		BESData data = new BESData(id, name, 1);
		_containersByID.put(new Long(id), data);
		_containersByName.put(name, data);
		_updateInformation.put(new Long(id), updateInfo = new BESUpdateInformation(
			id, _BES_UPDATE_CYCLE, _MISS_CAP));
		toUpdate.add(updateInfo);
		
		_logger.debug("Added new bes container \"" + name + 
			"\" into queue as resource " + id);
		
		updateResources(connection, toUpdate);
	}
	
	synchronized public Collection<EntryType> listBESs(
		Connection connection, Pattern pattern) 
		throws SQLException, ResourceException
	{
		HashMap<Long, EntryType> ret = new HashMap<Long, EntryType>();
		
		for (BESData data : _containersByID.values())
		{
			Matcher matcher = pattern.matcher(data.getName());
			if (matcher.matches())
			{
				ret.put(new Long(data.getID()),
					new EntryType(data.getName(), null, null));
			}
		}
		
		_database.fillInBESEPRs(connection, ret);
		return ret.values();
	}
	
	synchronized public Collection<String> removeBESs(Connection connection, 
		Pattern pattern) throws SQLException
	{
		Collection<String> ret = new LinkedList<String>();
		Collection<BESData> toRemove = new LinkedList<BESData>();
		
		for (BESData data : _containersByID.values())
		{
			Matcher matcher = pattern.matcher(data.getName());
			if (matcher.matches())
			{
				ret.add(data.getName());
				toRemove.add(data);
			}
		}
		
		_database.removeBESs(connection, toRemove);
		connection.commit();
		
		for (BESData data : toRemove)
		{
			_containersByID.remove(new Long(data.getID()));
			_containersByName.remove(data.getName());
			_scheduleableContainers.remove(new Long(data.getID()));
			_updateInformation.remove(new Long(data.getID()));

			_logger.debug("Removed bes container " + data.getID() + " from queue.");
		}
		
		return ret;
	}
	
	synchronized public void configureBES(Connection connection, String name, 
		int newSlots) throws SQLException, ResourceException
	{
		if (newSlots < 0)
			throw new IllegalArgumentException(
				"Not allowed to configure a container to " +
				"have LESS than 0 slots.");
		
		BESData data = _containersByName.get(name);
		if (data == null)
		{
			// We don't know about this container.
			throw new ResourceException("BES container \"" + name 
				+ "\" is unknown.");
		}
		
		_database.configureResource(connection, data.getID(), newSlots);
		connection.commit();
		int oldSlots = data.getTotalSlots();
		data.setTotalSlots(newSlots);
		
		_logger.debug("BES resource " + data.getID() + " configured to have "
			+ newSlots + " slots.");
		
		if (oldSlots < newSlots)
			_schedulingEvent.notifySchedulingEvent();
		
	}
	
	synchronized private void updateResources(Connection connection,
		Collection<BESUpdateInformation> resourcesToUpdate)
		throws SQLException, ResourceException, ConfigurationException,
			GenesisIISecurityException
	{
		for (BESUpdateInformation info : resourcesToUpdate)
		{
			ICallingContext queueCallingContext = _database.getQueueCallingContext(connection);
			IBESPortTypeResolver resolver = new BESPortTypeResolver(queueCallingContext);
			
			_outcallThreadPool.enqueue(new BESUpdateWorker(_connectionPool,
				this, info.getBESID(), resolver));
		}
	}
	
	synchronized public void updateResources(Connection connection)
		throws SQLException, ResourceException, ConfigurationException,
			GenesisIISecurityException
	{
		Collection<BESUpdateInformation> resourcesToUpdate = 
			new LinkedList<BESUpdateInformation>();
		Date now = new Date();
		
		for (BESUpdateInformation updateInfo : _updateInformation.values())
		{
			if (updateInfo.timeForUpdate(now))
			{
				resourcesToUpdate.add(updateInfo);
			}
		}
		
		updateResources(connection, resourcesToUpdate);
	}
	
	synchronized public void markBESAsAvailable(long besID)
	{
		BESUpdateInformation updateInfo = _updateInformation.get(
			new Long(besID));
		updateInfo.update(true);
		if (!_scheduleableContainers.containsKey(new Long(besID)));
		{
			BESData data = _containersByID.get(new Long(besID));
			_logger.info("Marking BES container \"" + data.getName() + "\" as available.");
			
			_scheduleableContainers.put(new Long(besID), data);
			if (data.getTotalSlots() > 0)
				_schedulingEvent.notifySchedulingEvent();
		}
	}
	
	synchronized public void markBESAsUnavailable(long besID)
	{
		BESUpdateInformation updateInfo = _updateInformation.get(
			new Long(besID));
		updateInfo.update(false);
		_scheduleableContainers.remove(new Long(besID));
		
		BESData data = _containersByID.get(new Long(besID));
		_logger.info("Marking BES container \"" + data.getName() + "\" as un-available.");
	}
	
	synchronized public Collection<BESData> getAvailableBESs()
	{
		return _scheduleableContainers.values();
	}
	
	private class BESPortTypeResolver implements IBESPortTypeResolver
	{
		private ICallingContext _callingContext;
		
		public BESPortTypeResolver(ICallingContext callingContext)
		{
			_callingContext = callingContext;
		}

		@Override
		public BESPortType createClientStub(Connection connection, long besID)
			throws Throwable
		{
			EntryType entry = new EntryType();
			HashMap<Long, EntryType> entries = new HashMap<Long, EntryType>();
			entries.put(new Long(besID), entry);
			_database.fillInBESEPRs(connection, entries);
			return ClientUtils.createProxy(BESPortType.class, 
				entry.getEntry_reference(), _callingContext);
		}
	}
}