package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public abstract class AbstractNativeQueueConnection 
	implements NativeQueueConnection
{
	transient private boolean _closed = false;
	private File _workingDirectory = null;
	private Map<URI, SPMDTranslator> _supportedSPMDVariations = 
		new HashMap<URI, SPMDTranslator>(4);
	private Properties _connectionProperties;
	private NativeQProperties _queueProperties;
	
	protected AbstractNativeQueueConnection(
		File workingDirectory, Properties connectionProperties) 
			throws NativeQueueException
	{
		_workingDirectory = workingDirectory;
		initialize(connectionProperties);
		addSupportedSPMDVariations(_supportedSPMDVariations, 
			connectionProperties);
		
		_connectionProperties = connectionProperties;
		_queueProperties = new NativeQProperties(_connectionProperties);
	}
	
	protected Properties connectionProperties()
	{
		return _connectionProperties;
	}
	
	protected OperatingSystemNames getOperatingSystem()
	{
		OperatingSystemTypeEnumeration ret = 
			_queueProperties.operatingSystemName();
		if (ret == null)
			return null;
		
		return OperatingSystemNames.valueOf(ret.getValue());
	}
	
	protected ProcessorArchitecture getProcessorArchitecture()
	{
		ProcessorArchitectureEnumeration ret =
			_queueProperties.cpuArchitecture();
		if (ret == null)
			return null;
		
		return ProcessorArchitecture.valueOf(ret.getValue());
	}
	
	protected File getCommonDirectory()
	{
		return _queueProperties.commonDirectory();
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