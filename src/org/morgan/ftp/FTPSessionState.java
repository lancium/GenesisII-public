package org.morgan.ftp;

import java.io.Closeable;
import java.io.IOException;

import org.morgan.util.io.StreamUtils;

public class FTPSessionState implements Closeable
{
	static final public int HISTORY_CAPACITY = 10;
	
	private FTPListenerManager _listenerManager;
	private FTPConfiguration _configuration;
	private RollingCommandHistory _commandHistory;
	private IBackend _backend;
	private int _sessionID;
	
	public FTPSessionState(FTPListenerManager listenerManager,
		FTPConfiguration configuration, IBackend backend, int sessionID)
	{
		_listenerManager = listenerManager;
		_commandHistory = new RollingCommandHistory(HISTORY_CAPACITY);
		_backend = backend;
		_sessionID = sessionID;
		_configuration = configuration;
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	public FTPConfiguration getConfiguration()
	{
		return _configuration;
	}
	
	public RollingCommandHistory getHistory()
	{
		return _commandHistory;
	}
	
	public IBackend getBackend()
	{
		return _backend;
	}
	
	public int getSessionID()
	{
		return _sessionID;
	}
	
	public FTPListenerManager getListenerManager()
	{
		return _listenerManager;
	}
	
	synchronized public void close() throws IOException
	{
		_commandHistory.close();
		
		if (_backend instanceof Closeable)
			StreamUtils.close((Closeable)_backend);
	}
}