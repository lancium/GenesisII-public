package org.morgan.ftp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.morgan.util.io.StreamUtils;

public class FTPDaemon
{
	static private Logger _logger = Logger.getLogger(FTPDaemon.class);
	
	private IBackendFactory _backendFactory;
	private FTPConfiguration _configuration;
	private ServerSocket _serverSocket = null;
	private SessionVulture _vulture;
	private FTPListenerManager _listenerManager;
	
	private Thread _serverThread = null;
	
	public FTPDaemon( 
			IBackendFactory backendFactory, FTPConfiguration configuration)
	{
		_listenerManager = new FTPListenerManager();
		_backendFactory = backendFactory;
		_configuration = configuration;
		
		_vulture = new SessionVulture();
		
		_listenerManager.addFTPListener(_vulture);
	}
	
	public void addFTPListener(FTPListener listener)
	{
		_listenerManager.addFTPListener(listener);
	}
	
	public void removeFTPListener(FTPListener listener)
	{
		_listenerManager.removeFTPListener(listener);
	}
	
	synchronized public void start() throws FTPException, IOException
	{
		if (_serverSocket != null)
			throw new FTPException("Server already running.");
		
		_serverSocket = new ServerSocket(_configuration.getListenPort());
		
		_serverThread = new Thread(new ServerThread());
		_serverThread.setName("FTP Daemon Server Thread");
		_serverThread.setDaemon(true);
		
		_serverThread.start();
	}
	
	synchronized public void stop() throws FTPException, IOException
	{
		if (_serverSocket == null)
			throw new FTPException("Server is not running.");
		
		_serverSocket.close();
		_serverSocket = null;
		
		StreamUtils.close(_vulture);
	}
	
	synchronized public boolean isRunning()
	{
		return _serverSocket != null;
	}
	
	private class ServerThread implements Runnable
	{
		public void run()
		{
			int nextSession = 0;
			
			try
			{
				while (true)
				{
					Socket socket = _serverSocket.accept();
					SocketAddress addr = socket.getRemoteSocketAddress();
					if (!(addr instanceof InetSocketAddress) ||
						!_configuration.connectionAllowed(addr))
					{
						_logger.error("Denied connection request from " + addr);
						
						StreamUtils.close(socket);
					} else
					{
						FTPSession session = new FTPSession(_listenerManager,
							_configuration, nextSession++, _backendFactory.newBackendInstance(), socket);

						_vulture.addSession(session);
						
						Thread th = new Thread(session, "FTP Session (" + (nextSession - 1) + ") Thread");
						th.setDaemon(true);
						th.start();
					}
				}
			}
			catch (IOException ioe)
			{
				_logger.error("Error accepting socket.", ioe);
			}
			finally
			{
				// MOOCH
				// closeAllSessions();
			}
		}
	}
}