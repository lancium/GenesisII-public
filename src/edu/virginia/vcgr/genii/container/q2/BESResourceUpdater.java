package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

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
	
	private class UpdaterWorker implements Runnable
	{
		public void run()
		{
			while (!_closed)
			{
				Connection connection = null;
				
				try
				{
					connection = _connectionPool.acquire();
					_manager.updateResources(connection);
					
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