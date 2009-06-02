package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

/**
 * This is the asynchronous worker class that periodically checks the status
 * of running jobs on a regular interval.
 * 
 * @author mmm2a
 */
public class JobStatusChecker
{
	static private Log _logger = LogFactory.getLog(JobStatusChecker.class);
	
	volatile private boolean _closed = false;
	private DatabaseConnectionPool _connectionPool;
	private JobManager _manager;
	private long _updateFrequency;
	
	public JobStatusChecker(DatabaseConnectionPool connectionPool,
		JobManager manager, long updateFrequency)
	{
		_connectionPool = connectionPool;
		_manager = manager;
		_updateFrequency = updateFrequency;
	
		/* Start the thread */
		Thread thread = new Thread(new UpdaterWorker());
		thread.setDaemon(true);
		thread.setName("Job Status Checker");
		
		thread.start();
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
	}
	
	/**
	 * This is an internal class that actually does the work for the thread.
	 * @author mmm2a
	 */
	private class UpdaterWorker implements Runnable
	{
		public void run()
		{
			while (!_closed)
			{
				Connection connection = null;
				
				try
				{
					/* Acquire a connection from the connection pool and ask
					 * the manager to check the job statuses.
					 */
					connection = _connectionPool.acquire(true);
					_manager.checkJobStatuses(connection);
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
					/* Now, wait an update cycle and repeat. */
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
