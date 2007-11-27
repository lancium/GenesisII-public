package org.morgan.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.morgan.util.io.StreamUtils;

public class SessionVulture implements Closeable, FTPListener
{
	static private final long _VULTURE_SLEEP_CYCLE = 1000 * 5;
	
	static private Logger _logger = Logger.getLogger(SessionVulture.class);
	
	private HashMap<Integer, FTPSession> _sessions = new HashMap<Integer, FTPSession>();
	private Thread _thread;
	
	public SessionVulture()
	{
		_thread = new Thread(new SessionWorker());
		_thread.setName("Session Vulture Thread");
		_thread.setDaemon(true);
		_thread.start();
	}
	
	public void addSession(FTPSession session)
	{
		synchronized(_sessions)
		{
			_sessions.put(new Integer(session.getSessionID()), session);
			_sessions.notify();
		}
	}
	
	public void removeSession(int sessionID)
	{
		synchronized(_sessions)
		{
			_sessions.remove(new Integer(sessionID));
		}
	}
	
	private class SessionWorker implements Runnable
	{
		public void run()
		{
			synchronized(_sessions)
			{
				try
				{
					while (true)
					{
						if (_sessions.isEmpty())
							_sessions.wait();
						else
							_sessions.wait(_VULTURE_SLEEP_CYCLE);
						
						for (FTPSession session : _sessions.values())
						{
							if (session.getIdleTime() >= session.getIdleTimeout())
							{
								_logger.info("Timing out idle session " + session.getSessionID());
								StreamUtils.close(session);
							}
						}
					}
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
	
	public void close() throws IOException
	{
		_thread.interrupt();
	}

	@Override
	public void sessionClosed(int sessionID)
	{
		removeSession(sessionID);
	}

	@Override
	public void sessionOpened(int sessionID)
	{
		// Nothing to do.
	}

	@Override
	public void userAuthenticated(int sessionID, String username)
	{
		// Nothing to do.
	}
}