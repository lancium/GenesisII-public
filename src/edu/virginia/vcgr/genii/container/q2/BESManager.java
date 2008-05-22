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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

/**
 * The BESManager is the main class for keeping track of and manipulating 
 * information about BES resources in the queue.
 * @author mmm2a
 */
public class BESManager implements Closeable
{
	/**
	 * BES resources are update every 5 minutes (unless they have historically
	 * been non-responsive, in which case the update cycle is 
	 * 5 minutes * 2^(MIN(numberOfMissedUpdates, 10)).
	 */
	static private final long _BES_UPDATE_CYCLE = 1000L * 60 * 5;
	
	/**
	 * The maximum number of misses to count against a bes container for 
	 * determinining it's exponential backoff for updates.
	 */
	static private final int _MISS_CAP = 10;
	
	static private Log _logger = LogFactory.getLog(BESManager.class);
	
	/** A simple boolean flag we use to signal to the update thread that it 
	 * should close. */
	volatile private boolean _closed = false;
	
	private QueueDatabase _database;
	
	/**
	 * The scheduling event is an object which allows code to raise and wait
	 * on events indicating that a good opportunity exists to schedule a job.
	 * Opportunities are things like:
	 * <UL>
	 * <LI>A container was given more slots to use</LI>
	 * <LI>A new container was added</LI>
	 * <LI>A new job was added</LI>
	 * <LI>A job finished or was re-queued</LI>
	 * <LI>...</LI>
	 * </UL>
	 */
	private SchedulingEvent _schedulingEvent;
	
	private DatabaseConnectionPool _connectionPool;
	
	/** A map of container IDs to their in-memory data. */
	private HashMap<Long, BESData> _containersByID = 
		new HashMap<Long, BESData>();
	
	/** A map of container names to their in-memory data. */
	private HashMap<String, BESData> _containersByName = 
		new HashMap<String, BESData>();
	
	/** A map of container ID to update information.  Right now this
	 * is basically information about when it was updated and what the
	 * result was, but eventually it could help us determine what the
	 * "characteristics" of that container are.
	 */
	private HashMap<Long, BESUpdateInformation> _updateInformation = 
		new HashMap<Long, BESUpdateInformation>();
	
	/**
	 * This list gives all of the containers which are currently responsive.
	 * Note that it DOES NOT indicate that you have slots available, only that
	 * you are responsive to messages.
	 */
	private HashMap<Long, BESData> _scheduleableContainers = 
		new HashMap<Long, BESData>();
	
	/**
	 * Thread pool used to make out-calls from the queue.
	 */
	private ThreadPool _outcallThreadPool;
	
	/**
	 * This is the worker object (java.lang.Runnable) used to request updates
	 * from BES containers.
	 */
	private BESResourceUpdater _updater;
	
	public BESManager(ThreadPool outcallThreadPool, QueueDatabase database, 
		SchedulingEvent schedulingEvent, Connection connection, 
		DatabaseConnectionPool connectionPool) 
			throws SQLException, ResourceException, 
				GenesisIISecurityException
	{
		_connectionPool = connectionPool;
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		
		loadFromDatabase(connection);
		
		/* Create the updater worker.  This object will continually update 
		 * the bes resources on a regular basis.  We know what the update
		 * frequency is from a constant, but we'll actually have the "updater"
		 * object wake up 10 times as often to check.  This is essentially me
		 * being lazy but its a lot easier to have the guy wake up frequently
		 * and check to see if anyone needs an update. */
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
	
	/**
	 * Load all BES containers currently stored in the database.  This should 
	 * only be called once each time when the web service container starts up.
	 * 
	 * @param connection The database connection to use.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationExceptionMOOCH
	 * @throws GenesisIISecurityException
	 */
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException, ResourceException, 
			GenesisIISecurityException
	{
		/* Ask the database manager for all BES records located 
		 * in the database */
		Collection<BESData> allBESs = _database.loadAllBESs(connection);
		
		/* For each record, put the record into the correct map */
		for (BESData bes : allBESs)
		{
			_containersByID.put(new Long(bes.getID()), bes);
			_containersByName.put(bes.getName(), bes);
			_updateInformation.put(new Long(bes.getID()), 
				new BESUpdateInformation(bes.getID(), _BES_UPDATE_CYCLE, _MISS_CAP));
		}
		
		/* Go ahead and update the bes containers right now at the beginning to
		 * get the "ball rolling" on updates.
		 */
		updateResources(connection);
	}
	
	/**
	 * Add a new BES container into the queue.
	 * 
	 * @param connection The database connection to use.
	 * @param name The name to link the new BES container into the queue with.
	 * @param epr The EPR of the bes container.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationExceptionMOOCH
	 * @throws GenesisIISecurityException
	 */
	synchronized public void addNewBES(Connection connection, String name,
			EndpointReferenceType epr)
		throws SQLException, ResourceException,
			GenesisIISecurityException
	{
		BESUpdateInformation updateInfo;
		Collection<BESUpdateInformation> toUpdate = 
			new ArrayList<BESUpdateInformation>(1);
		
		/* Add the new information into the database and get back the 
		 * new DB key for it. */
		long id = _database.addNewBES(connection, name, epr);
		connection.commit();
		
		/* We've committed to the DB, if we fail now, it'll get loaded into 
		 * memory when we restart. */
		
		/* Go ahead and put the "in-memory" information into the 
		 * correct lists. */
		BESData data = new BESData(id, name, 1);
		_containersByID.put(new Long(id), data);
		_containersByName.put(name, data);
		_updateInformation.put(new Long(id), updateInfo = new BESUpdateInformation(
			id, _BES_UPDATE_CYCLE, _MISS_CAP));
		
		/* Finally, go ahead and kick off an update of this information.  By 
		 * doing this early, we could potentially get a near simultaneous 
		 * update from our update thread, but it's OK...two updates won't 
		 * hurt us. */
		toUpdate.add(updateInfo);
		updateResources(connection, toUpdate);
		
		_logger.debug("Added new bes container \"" + name + 
			"\" into queue as resource " + id);
	}
	
	/**
	 * List all BES's contained in the queue which match a given 
	 * regular expression.
	 * 
	 * @param connection The database connection to use.
	 * @param pattern The regular expression to match entries against.
	 * @return The list of bes entries contained in the queue with the
	 * given pattern.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public Collection<EntryType> listBESs(
		Connection connection, String entryName) 
		throws SQLException, ResourceException
	{
		HashMap<Long, EntryType> ret = new HashMap<Long, EntryType>();
		
		/* Go through all bes containers in the queue, looking for 
		 * the right ones. */
		for (BESData data : _containersByID.values())
		{
			/* See if this container matches the pattern */
			if (entryName == null || entryName.equals(data.getName()))
			{
				/* If so, add it's entry information (but leave the EPR 
				 * blank, we'll back-fill that in a second. */
				ret.put(new Long(data.getID()),
					new EntryType(data.getName(), null, null));
			}
		}
		
		/* Now that we have all of the entries we want to return, back-fill
		 * the EPRs in those entries by going to the database.
		 */
		_database.fillInBESEPRs(connection, ret);
		return ret.values();
	}
	
	/**
	 * Remove BES containers from a queue based off of regular expression.
	 * 
	 * @param connection The database connection.
	 * @param pattern The regular expression to match with.
	 * @return The list of bes containers removed by this operation.
	 * 
	 * @throws SQLException
	 */
	synchronized public Collection<String> removeBESs(Connection connection, 
		String entryName) throws SQLException
	{
		Collection<String> ret = new LinkedList<String>();
		Collection<BESData> toRemove = new LinkedList<BESData>();
		
		/* As with list, find all containers that match the given pattern */
		for (BESData data : _containersByID.values())
		{
			if (entryName == null || entryName.equals(data.getName()))
			{
				/* We found a match.  Add the name to a return list.  We'll
				 * also keep a list of matching data structures for containers
				 * that we have to delete from the database in a second.
				 */
				ret.add(data.getName());
				toRemove.add(data);
			}
		}
		
		/* Now that we have all of the containers that matched, go ahead
		 * and remove all of them from the database.
		 */
		_database.removeBESs(connection, toRemove);
		connection.commit();
		
		/* It's been committed.  Everything after this can be restored 
		 * from DB if the JVM crashes or the container goes down. */
		
		/* Now we have to go through all of our in-memory lists and 
		 * remove the records */
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
	
	/**
	 * Configure a bes container to have a (potentially) different number of 
	 * slots allocated to it.
	 * 
	 * @param connection The database connection to use.
	 * @param name The name of the bes container to configure.
	 * @param newSlots The number of slots to allocate to it 
	 * (must be non-negative).
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	synchronized public void configureBES(Connection connection, String name, 
		int newSlots) throws SQLException, ResourceException
	{
		/* Check the pre-conditions on the number of slots */
		if (newSlots < 0)
			throw new IllegalArgumentException(
				"Not allowed to configure a container to " +
				"have LESS than 0 slots.");
		
		/* Find the container we are altering. */
		BESData data = _containersByName.get(name);
		if (data == null)
		{
			// We don't know about this container.
			throw new ResourceException("BES container \"" + name 
				+ "\" is unknown.");
		}
		
		/* Update the information in the database */
		_database.configureResource(connection, data.getID(), newSlots);
		connection.commit();
		
		/* The update information has been committed to the DB.  
		 * We can now restore from load if necessary. */
		
		/* set the in memory representation of the slots 
		 * (and keep track of the old one) */
		int oldSlots = data.getTotalSlots();
		data.setTotalSlots(newSlots);
		
		_logger.debug("BES resource " + data.getID() + " configured to have "
			+ newSlots + " slots.");
		
		/* If we increased the number of slots, we have a 
		 * scheduling opportunity here */
		if (oldSlots < newSlots)
			_schedulingEvent.notifySchedulingEvent();
		
	}
	
	/**
	 * Make out-calls to the indicated resources to get their latest 
	 * update information.
	 * 
	 * @param connection The database connection.
	 * @param resourcesToUpdate The list of bes containers to update.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationExceptionMOOCH
	 * @throws GenesisIISecurityException
	 */
	synchronized private void updateResources(Connection connection,
		Collection<BESUpdateInformation> resourcesToUpdate)
		throws SQLException, ResourceException,
			GenesisIISecurityException
	{
		/* The queue uses the same calling context for ALL BES updates 
		 * (it's own).  Load that context from the database. */
		ICallingContext queueCallingContext = _database.getQueueCallingContext(
			connection);
		
		/* Go through the list of containers to update */
		for (BESUpdateInformation info : resourcesToUpdate)
		{
			/* Create a "helper" object which contains enough information for 
			 * the updater worker to "resolve" the bes id into it's EPR.  
			 * This allows us to delay loading the EPR into memory (a 
			 * potentially memory-expensive prospect) until the last possible 
			 * minute.  This way, we guarantee that the maximum number of 
			 * EPRs loaded at any time is equal to the maximum number of
			 * out call threads in use.
			 */
			IBESPortTypeResolver resolver = new BESPortTypeResolver(
				queueCallingContext);
			
			/* Go ahead and enqueue a new "update" worker into the outcall 
			 * thread pool. */
			_outcallThreadPool.enqueue(new BESUpdateWorker(_connectionPool,
				this, info.getBESID(), resolver));
		}
	}
	
	/**
	 * Update ALL resources (that need the update) currently linked into 
	 * the queue.
	 * 
	 * @param connection The database connection.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 * @throws ConfigurationExceptionMOOCH
	 * @throws GenesisIISecurityException
	 */
	synchronized public void updateResources(Connection connection)
		throws SQLException, ResourceException,
			GenesisIISecurityException
	{
		/* We need to collect a list of BES resources that are ready for
		 * an update.
		 */
		Collection<BESUpdateInformation> resourcesToUpdate = 
			new LinkedList<BESUpdateInformation>();
		
		/* Get the current timestamp */
		Date now = new Date();
		
		/* Loop through all containers (their update information 
		 * structures actually) */
		for (BESUpdateInformation updateInfo : _updateInformation.values())
		{
			/* If this container is ready for an update, go ahead and add it
			 * to the "to-update" list.
			 */
			if (updateInfo.timeForUpdate(now))
				resourcesToUpdate.add(updateInfo);
		}
		
		/* Now, call update on all of the resources ready for an update */
		updateResources(connection, resourcesToUpdate);
	}
	
	/**
	 * Mark a resource as being available.
	 * 
	 * @param besID The resource's key
	 */
	synchronized public void markBESAsAvailable(long besID)
	{
		/* Get it's update information from memory and note that it's
		 * now responsive and available. */
		BESUpdateInformation updateInfo = _updateInformation.get(
			new Long(besID));
		updateInfo.update(true);
		
		/* See if that resource is currently listed in the "available" 
		 * resources list. */
		if (!_scheduleableContainers.containsKey(new Long(besID)));
		{
			/* If it isn't, we need to add it to that list.  Get's it's
			 * in memory data structure and put the data structure into
			 * the available list.
			 */
			BESData data = _containersByID.get(new Long(besID));
			_logger.info("Marking BES container \"" + data.getName() + "\" as available.");
			
			_scheduleableContainers.put(new Long(besID), data);
			
			/* Finally, if the thing had more then 0 slots allocated to it,
			 * the fact that it is now available (and wasn't before) means that
			 * we have a new scheduling opportunity.
			 */
			if (data.getTotalSlots() > 0)
				_schedulingEvent.notifySchedulingEvent();
		}
	}
	
	/**
	 * Similar to markBESAsAvailable, we are marking a resource as
	 * unavailable.  Unavailable doesn't mean that the container isn't
	 * responding.  It simply means that the container won't take jobs
	 * right now for some reason.
	 * 
	 * @param besID The key of the resource that we need to mark as
	 * unavailable.
	 */
	synchronized public void markBESAsUnavailable(long besID)
	{
		/* Find it's update information structure and mark it as down */
		BESUpdateInformation updateInfo = _updateInformation.get(
			new Long(besID));
		updateInfo.update(false);
		
		/* Remove the structure from the available list (if it isn't there, 
		 * this operation is a no-op. */
		_scheduleableContainers.remove(new Long(besID));
		
		/* For debugging purposes, we'll get enough information to print out 
		 * it's name. This costs us a little time, but not enough to worry 
		 * about. */
		BESData data = _containersByID.get(new Long(besID));
		_logger.info("Marking BES container \"" + data.getName() 
			+ "\" as un-available.");
	}
	
	/**
	 * Similar to markBESAsAvailable, we are marking a resource as
	 * missed.  Missed implies that we couldn't talk to the container
	 * at all (it isn't responsive).
	 * 
	 * @param besID The key of the resource that we need to mark as
	 * missed.
	 */
	synchronized public void markBESAsMissed(long besID)
	{
		BESUpdateInformation updateInfo = _updateInformation.get(
			new Long(besID));
		updateInfo.miss();
		
		/* Remove the structure from the available list (if it isn't there, 
		 * this operation is a no-op. */
		_scheduleableContainers.remove(new Long(besID));
		
		/* For debugging purposes, we'll get enough information to print out 
		 * it's name. This costs us a little time, but not enough to worry 
		 * about. */
		BESData data = _containersByID.get(new Long(besID));
		_logger.info("Marking BES container \"" + data.getName() 
			+ "\" as un-responsive.");
	}
	
	/**
	 * Simple getter method to return the list of all 
	 * currently available resources.
	 * 
	 * @return The list of available resources.
	 */
	synchronized public Collection<BESData> getAvailableBESs()
	{
		return _scheduleableContainers.values();
	}
	
	/**
	 * An internal class that we use to "late-bind" BES keys to
	 * the BES container's EPR.  This allows us to avoid ever
	 * loading an excessive number of EPRs into memory at any given
	 * time.
	 * 
	 * @author mmm2a
	 */
	private class BESPortTypeResolver implements IBESPortTypeResolver
	{
		/**
		 * The calling context that we will use to make out calls.  This
		 * needs to get used during the client-stub's creation.
		 */
		private ICallingContext _callingContext;
		
		public BESPortTypeResolver(ICallingContext callingContext)
		{
			_callingContext = callingContext;
		}

		/* See interface declaration */
		@Override
		public GeniiBESPortType createClientStub(Connection connection, long besID)
			throws Throwable
		{
			/* We need to get the EPR of this BES container.  The database
			 * already has a method to do this by filling in EntryType
			 * instances (used by the list method).  We'll just re-use
			 * that operation by creating a faux entry type.
			 */
			EntryType entry = new EntryType();
			HashMap<Long, EntryType> entries = new HashMap<Long, EntryType>();
			entries.put(new Long(besID), entry);
			_database.fillInBESEPRs(connection, entries);
			
			/* Go ahead and create the client stub with the calling context and
			 * the EPR.
			 */
			return ClientUtils.createProxy(GeniiBESPortType.class, 
				entry.getEntry_reference(), _callingContext);
		}
	}
}