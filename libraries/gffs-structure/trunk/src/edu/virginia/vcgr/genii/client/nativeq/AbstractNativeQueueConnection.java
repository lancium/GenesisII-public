package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;

public abstract class AbstractNativeQueueConnection<ProviderConfigType> implements NativeQueueConnection
{
	transient private boolean _closed = false;
	private File _workingDirectory = null;
	private ResourceOverrides _resourceOverrides;
	private CmdLineManipulatorConfiguration _manipulatorConfiguration;
	private NativeQueueConfiguration _queueConfiguration;
	private ProviderConfigType _providerConfiguration;

	protected AbstractNativeQueueConnection(File workingDirectory, ResourceOverrides resourceOverrides,
		CmdLineManipulatorConfiguration cmdLineManipulatorConf, NativeQueueConfiguration queueConfig,
		ProviderConfigType providerConfig) throws NativeQueueException
	{
		_workingDirectory = workingDirectory;
		_resourceOverrides = resourceOverrides;
		_manipulatorConfiguration = cmdLineManipulatorConf;
		_queueConfiguration = queueConfig;
		_providerConfiguration = providerConfig;

		initialize();
	}

	protected ResourceOverrides resourceOverrides()
	{
		return _resourceOverrides;
	}

	protected CmdLineManipulatorConfiguration cmdLineManipulatorConf()
	{
		return _manipulatorConfiguration;
	}

	protected NativeQueueConfiguration queueConfiguration()
	{
		return _queueConfiguration;
	}

	protected ProviderConfigType providerConfiguration()
	{
		return _providerConfiguration;
	}

	protected File getCommonDirectory()
	{
		return _queueConfiguration.sharedDirectory();
	}

	protected void initialize() throws NativeQueueException
	{
	}

	protected void finalize() throws Throwable
	{
		super.finalize();

		close();
	}

	protected void checkBinary(File binaryPath) throws NativeQueueException
	{
		if (!binaryPath.exists())
			throw new NativeQueueException("Binary \"" + binaryPath.getAbsolutePath() + "\" does not exist.");

		if (!binaryPath.canExecute())
			throw new NativeQueueException("Binary \"" + binaryPath.getAbsolutePath() + "\" is not executable.");
	}

	protected File getWorkingDirectory()
	{
		return _workingDirectory;
	}

	@Override
	synchronized public void close() throws IOException
	{
		if (!_closed) {
			try {
				closeConnection();
			} catch (NativeQueueException nqe) {
				throw new IOException("Unable to close queue connection.", nqe);
			} finally {
				_closed = true;
			}
		}
	}

	protected void closeConnection() throws NativeQueueException
	{
	}
}