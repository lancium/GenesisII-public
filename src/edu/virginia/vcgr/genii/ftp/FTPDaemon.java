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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class FTPDaemon extends Thread implements IFTPListener
{
	static private Log _logger = LogFactory.getLog(FTPDaemon.class);
	
	private IdleSessionVulture _vulture = new IdleSessionVulture();
	
	private ArrayList<IFTPListener> _listeners 
		= new ArrayList<IFTPListener>();
	
	private ICallingContext _rootContext;
	private ServerSocket _sSock;
	private FTPConfiguration _configuration;
	
	private ArrayList<FtpSession> _activeSessions = new ArrayList<FtpSession>();
	
	public void addFTPListener(IFTPListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public void removeFTPListener(IFTPListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	private IFTPListener[] getListeners()
	{
		IFTPListener []listeners;
		
		synchronized (_listeners)
		{
			listeners = new IFTPListener[_listeners.size()];
			_listeners.toArray(listeners);
		}
		
		return listeners;
	}
	
	protected void fireFTPSessionCreated(int sessionID, 
		InetAddress remoteAddress)
	{
		IFTPListener []listeners = getListeners();
		for (IFTPListener listener : listeners)
		{
			listener.sessionCreated(sessionID, remoteAddress);
		}
	}
	
	public FTPDaemon(ICallingContext rootContext, FTPConfiguration conf)
		throws IOException, ConfigurationException, RNSException
	{
		super("VCGRFtp Server");
		setDaemon(false);
		
		_configuration = conf;
		
		_sSock = new ServerSocket(conf.getListenPort());
		_rootContext = rootContext.deriveNewContext();
		
		RNSPath sandBox = _rootContext.getCurrentPath().getRoot().lookup(
			_configuration.getSandboxPath(), RNSPathQueryFlags.MUST_EXIST);
		
		_rootContext.setCurrentPath(sandBox);
		
		_vulture.start();
	}
	
	public FTPDaemon(FTPConfiguration conf)
		throws IOException, ConfigurationException, RNSException
	{
		this(ContextManager.getCurrentContext(), conf);
	}
	
	public void run()
	{
		int nextSession = 0;
		
		try
		{
			while (true)
			{
				Socket socket = _sSock.accept();
				
				SocketAddress addr = socket.getRemoteSocketAddress();
				if (!(addr instanceof InetSocketAddress) ||
					!_configuration.connectionAllowed(addr))
				{
					_logger.error(
						"Denied connection request from " + addr);
					try { socket.close(); } catch (Throwable t) {}
				} else
				{
					InetSocketAddress iaddr = (InetSocketAddress)addr;
					
					fireFTPSessionCreated(
						nextSession, iaddr.getAddress());
					FtpSession session = new FtpSession(
						_configuration,
						getListeners(), nextSession++,
						_rootContext.deriveNewContext(), socket);
					_vulture.addReapable(session);
					synchronized(_activeSessions)
					{
						_activeSessions.add(session);
					}
					
					session.start();
				}
			}
		}
		catch (IOException ioe)
		{
		}
	}
	
	public void shutdown()
	{
		try { _sSock.close(); } catch (Throwable t) {}
		
		FtpSession []sessions;
		synchronized(_activeSessions)
		{
			sessions = new FtpSession[_activeSessions.size()];
			_activeSessions.toArray(sessions);
		}
		
		for (FtpSession session : sessions)
		{
			session.close();
		}
	}

	public void sessionCreated(int sessionID, InetAddress remoteAddress)
	{
	}

	public void sessionClosed(int sessionID)
	{
		ArrayList<FtpSession> removeable = new ArrayList<FtpSession>();
		
		synchronized (_activeSessions)
		{
			for (FtpSession session : _activeSessions)
			{
				if (session.getSessionID() == sessionID)
					removeable.add(session);
			}
			
			for (FtpSession session : removeable)
			{
				_activeSessions.remove(session);
			}
		}
		
	}

	public void userLoggedIn(int sessionID, String username) 
	{
	}
}
