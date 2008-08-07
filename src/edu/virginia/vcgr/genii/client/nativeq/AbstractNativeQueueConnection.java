package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;

public abstract class AbstractNativeQueueConnection 
	implements NativeQueueConnection
{
	transient private boolean _closed = false;
	private File _workingDirectory = null;
	private Map<URI, SPMDTranslator> _supportedSPMDVariations = 
		new HashMap<URI, SPMDTranslator>(4);
	
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
		Map<URI, SPMDTranslator> variations, Properties connectionProperties)
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
	public Map<URI, SPMDTranslator> supportedSPMDVariations()
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