/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.ftp;

import java.net.SocketAddress;

public class FTPConfiguration
{
	private int _listPort;
	private int _idleTimeoutSeconds;
	private int _dataConnectionTimeout;
	private int _missedAuthenticationsLimit;
	private String _sandboxPath;
	private NetworkConstraint []_networkConstraints;
	
	public FTPConfiguration(int listenPort)
	{
		_listPort = listenPort;
		_idleTimeoutSeconds = 150;
		_dataConnectionTimeout = 60;
		_missedAuthenticationsLimit = 10;
		_sandboxPath = "/";
		
		_networkConstraints = null;
	}
	
	public int getListenPort()
	{
		return _listPort;
	}
	
	public int getIdleTimeoutSeconds()
	{
		return _idleTimeoutSeconds;
	}
	
	public int getDataConnectionTimeoutSeconds()
	{
		return _dataConnectionTimeout;
	}
	
	public int getMissedAuthentiationsLimit()
	{
		return _missedAuthenticationsLimit;
	}
	
	public String getSandboxPath()
	{
		return _sandboxPath;
	}
	
	public boolean connectionAllowed(SocketAddress addr)
	{
		if (_networkConstraints == null ||
			_networkConstraints.length == 0)
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
		_listPort = port;
	}
	
	public void setIdleTimeoutSeconds(int timeoutSecs)
	{
		_idleTimeoutSeconds = timeoutSecs;
	}
	
	public void setDataConnectionTimeoutSeconds(int timeoutSecs)
	{
		_dataConnectionTimeout = timeoutSecs;
	}
	
	public void setMissedAuthentiationsLimit(int missLimit)
	{
		_missedAuthenticationsLimit = missLimit;
	}
	
	public void setSandboxPath(String newPath)
	{
		_sandboxPath = newPath;
	}
	
	public void setNetworkConstraints(NetworkConstraint []constraints)
	{
		_networkConstraints = constraints;
	}
}
