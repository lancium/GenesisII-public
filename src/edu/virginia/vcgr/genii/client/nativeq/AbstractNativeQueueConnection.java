package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractNativeQueueConnection 
	implements NativeQueueConnection
{
	transient private boolean _closed = false;
	private File _workingDirectory = null;
	private Set<URI> _supportedSPMDVariations = new HashSet<URI>(4);
	
	protected AbstractNativeQueueConnection(
		File workingDirectory, Properties connectionProperties) 
			throws NativeQueueException
	{
		_workingDirectory = workingDirectory;
		initialize(connectionProperties);
		addSupportedSPMDVariations(_supportedSPMDVariations, 
			connectionProperties);
	}
	
	protected void initialize(Properties connectionProperties)
		throws NativeQueueException
	{
	}
	
	protected void addSupportedSPMDVariations(
		Set<URI> variations, Properties connectionProperties)
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
	public Set<URI> supportedSPMDVariations()
	{
		return _supportedSPMDVariations;
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