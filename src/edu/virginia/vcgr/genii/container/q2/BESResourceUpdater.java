package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

/**
 * This class is a worker class that is used by the BESResourceManager to
 * automatically update the BES resources on a regular interval.
 * 
 * @author mmm2a
 */
public class BESResourceUpdater implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESResourceUpdater.class);
	
	volatile private boolean _closed = false;
	private DatabaseConnectionPool _connectionPool;
	private BESManager _manager;
	private long _updateFrequency;
	private Thread _thread;
	
	public BESResourceUpdater(DatabaseConnectionPool connectionPool,
		BESManager manager, long updateFrequency)
	{
		_connectionPool = connectionPool;
		_manager = manager;
		_updateFrequency = updateFrequency;
		
		/* Start a new thread to regularly update (and sleep in between) */
		_thread = new Thread(new UpdaterWorker());
		_thread.setDaemon(true);
		_thread.setName("BES Updater Worker");
		
		_thread.start();
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		_thread.interrupt();
	}
	
	/* The update worker that the thread is using */
	private class UpdaterWorker implements Runnable
	{
		public void run()
		{
			while (!_closed)
			{
				Connection connection = null;
				
				try
				{
					/* Acquire a new connection from the pool */
					connection = _connectionPool.acquire();
					
					/* Have the BES manager update the resources */
					_manager.updateResources(connection);
					
					/* Wait for the next update cycle */
					Thread.sleep(_updateFrequency);
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
				catch (Throwable cause)
				{
					_logger.warn(
						"Unable to update BES resources in queue.", cause);
				}
				finally
				{
					_connectionPool.release(connection);
				}
			}
		}
	}
}