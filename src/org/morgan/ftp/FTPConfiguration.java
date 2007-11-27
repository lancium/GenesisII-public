package org.morgan.ftp;

import java.net.SocketAddress;

public class FTPConfiguration
{
	private int _listenPort;
	private int _idleTimeoutSeconds;
	private int _dataConnectionTimeout;
	private int _missedAuthenticationsLimit;
	private NetworkConstraint []_networkConstraints;
	
	public FTPConfiguration(int listenPort)
	{
		_listenPort = listenPort;
		_idleTimeoutSeconds = 150;
		_dataConnectionTimeout = 60;
		_missedAuthenticationsLimit = 3;
		
		_networkConstraints = null;
	}
	
	public int getListenPort()
	{
		return _listenPort;
	}
	
	public int getIdleTimeoutSeconds()
	{
		return _idleTimeoutSeconds;
	}
	
	public int getDataConnectionTimeoutSeconds()
	{
		return _dataConnectionTimeout;
	}
	
	public int getMissedAuthenticationsLimit()
	{
		return _missedAuthenticationsLimit;
	}
	
	public boolean connectionAllowed(SocketAddress addr)
	{
		if (_networkConstraints == null)
			return true;
		
		for (NetworkConstraint constraint : _networkConstraints)
		{
			if (constraint.matches(addr))
				return true;
		}
		
		return false;
	}
	
	public void setListenPort(int port)
	{
		_listenPort = port;
	}
	
	public void setIdleTimeoutSeconds(int timeoutSeconds)
	{
		_idleTimeoutSeconds = timeoutSeconds;
	}
	
	public void setDataConnectionTimeoutSeconds(int timeoutSeconds)
	{
		_dataConnectionTimeout = timeoutSeconds;
	}
	
	public void setMissedAuthenticationsLimit(int missLimit)
	{
		_missedAuthenticationsLimit = missLimit;
	}
	
	public void setNetworkConstraints(NetworkConstraint []constraints)
	{
		_networkConstraints = constraints;
	}
}