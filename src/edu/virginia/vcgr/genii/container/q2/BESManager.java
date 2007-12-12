package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class BESManager implements Closeable
{
	static private final long _BES_UPDATE_CYCLE = 1000L * 60 * 5;
	
	static private Log _logger = LogFactory.getLog(BESManager.class);
	
	volatile private boolean _closed = false;
	
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	
	private HashMap<Long, BESData> _containersByID = 
		new HashMap<Long, BESData>();
	private HashMap<String, BESData> _containersByName = 
		new HashMap<String, BESData>();
	private HashMap<Long, BESUpdateInformation> _updateInformation = 
		new HashMap<Long, BESUpdateInformation>();
	private HashMap<Long, BESData> _scheduleableContainers = 
		new HashMap<Long, BESData>();
	
	public BESManager(QueueDatabase database, SchedulingEvent schedulingEvent,
		Connection connection) throws SQLException
	{
		_database = database;
		_schedulingEvent = schedulingEvent;
		
		loadFromDatabase(connection);
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
	}
	
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException
	{
		Collection<BESData> allBESs = _database.loadAllBESs(connection);
		
		for (BESData bes : allBESs)
		{
			_containersByID.put(new Long(bes.getID()), bes);
			_containersByName.put(bes.getName(), bes);
			_updateInformation.put(new Long(bes.getID()), 
				new BESUpdateInformation(bes.getID(), _BES_UPDATE_CYCLE));
		}
	}
	
	synchronized public void addNewBES(Connection connection, String name,
			EndpointReferenceType epr) throws SQLException, ResourceException
	{
		long id = _database.addNewBES(connection, name, epr);
		connection.commit();
		BESData data = new BESData(id, name, 1);
		_containersByID.put(new Long(id), data);
		_containersByName.put(name, data);
		_updateInformation.put(new Long(id), new BESUpdateInformation(
			id, _BES_UPDATE_CYCLE));
		
		_logger.debug("Added new bes container \"" + name + 
			"\" into queue as resource " + id);
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
}