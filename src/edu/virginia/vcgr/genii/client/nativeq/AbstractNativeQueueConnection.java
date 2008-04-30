package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractNativeQueueConnection 
	implements NativeQueueConnection
{
	transient private boolean _closed = false;
	private File _workingDirectory = null;
	
	protected AbstractNativeQueueConnection(
		File workingDirectory, Properties connectionProperties) 
			throws NativeQueueException
	{
		_workingDirectory = workingDirectory;
		initialize(connectionProperties);
	}
	
	protected void initialize(Properties connectionProperties)
		throws NativeQueueException
	{
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	protected void checkBinary(File binaryPath)
		throws NativeQueueException
	{
		if (!binaryPath.exists())
			throw new NativeQueueException("Binary \"" + 
				binaryPath.getAbsolutePath() + "\" does not exist.");
		
		if (!binaryPath.canExecute())
			throw new NativeQueueException("Binary \"" + 
				binaryPath.getAbsolutePath() + "\" is not executable.");
	}
	
	protected File getWorkingDirectory()
	{
		return _workingDirectory;
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (!_closed)
		{
			try
			{
				closeConnection();
			}
			catch (NativeQueueException nqe)
			{
				throw new IOException("Unable to close queue connection.", 
					nqe);
			}
			finally
			{			
				_closed = true;
			}
		}
	}
	
	protected void closeConnection() 
		throws NativeQueueException
	{
	}
}