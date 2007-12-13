package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class JobStatusChecker
{
	static private Log _logger = LogFactory.getLog(JobStatusChecker.class);
	
	volatile private boolean _closed = false;
	private DatabaseConnectionPool _connectionPool;
	private JobManager _manager;
	private long _updateFrequency;
	private Thread _thread;
	
	public JobStatusChecker(DatabaseConnectionPool connectionPool,
		JobManager manager, long updateFrequency)
	{
		_connectionPool = connectionPool;
		_manager = manager;
		_updateFrequency = updateFrequency;
		
		_thread = new Thread(new UpdaterWorker());
		_thread.setDaemon(true);
		_thread.setName("Job Status Checker");
		
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
					_manager.checkJobStatuses(
						_connectionPool, connection);
				}
				catch (Throwable cause)
				{
					_logger.warn(
						"Unable to check job statuses in queue.", cause);
				}
				finally
				{
					_connectionPool.release(connection);
				}
				
				try
				{
					Thread.sleep(_updateFrequency);
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
	}
}
