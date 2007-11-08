package edu.virginia.vcgr.ogrsh.server.session;

import java.io.Closeable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.morgan.util.GUID;

import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public class SessionManager implements Closeable
{
	static private class SessionBundle
	{
		private Session _session;
		private int _referenceCount;
		private Date _idleStart;
		
		public SessionBundle(Session session)
		{
			_session = session;
			_referenceCount = 0;
			_idleStart = new Date();
		}
		
		public Session getSession()
		{
			return _session;
		}
		
		public void incrementCount()
		{
			if (++_referenceCount > 0)
				_idleStart = null;
		}
		
		public void decrementCount()
		{
			if (--_referenceCount <= 0)
				_idleStart = new Date();
		}
		
		public boolean isScavengeable(long minIdleTime)
		{
			if (_idleStart == null)
				return false;
			
			Date now = new Date();
			return (now.getTime() - _idleStart.getTime() >= minIdleTime);
		}
	}
	
	static private final long _DEFAULT_MIN_IDLE_TIME = 30 * 1000L;	// 30 seconds
	
	private HashMap<GUID, SessionBundle> _sessions = new HashMap<GUID, SessionBundle>();
	private long _minIdleTime;
	private Thread _scavengerThread;
	private boolean _closed = false;
	
	public SessionManager(long minIdleTime)
	{
		_minIdleTime = minIdleTime;
		
		_scavengerThread = new Thread(new SessionScavenger());
		_scavengerThread.setDaemon(false);
		_scavengerThread.start();
	}
	
	public SessionManager()
	{
		this(_DEFAULT_MIN_IDLE_TIME);
	}
	
	protected void finalize()
	{
		close();
	}
	
	public Session createNewSession()
	{
		Session session;
		
		synchronized(_sessions)
		{
			while (true)
			{
				session = new Session();
				if (_sessions.containsKey(session.getSessionID()))
					continue;
				
				SessionBundle bundle = new SessionBundle(session);
				bundle.incrementCount();
				
				_sessions.put(session.getSessionID(), bundle);
				break;
			}
		}
		
		return session;
	}
	
	public Session duplicateSession(GUID sessionID)
		throws OGRSHException
	{
		Session session;
		
		synchronized(_sessions)
		{
			SessionBundle bundle = _sessions.get(sessionID);
			if (bundle == null)
				throw new OGRSHException(OGRSHException.UNKNOWN_SESSION_EXCEPTION,
					"Session \"" + sessionID + "\" is not recognized.");
			session = bundle.getSession();
			session = session.duplicate();
			bundle = new SessionBundle(session);
			bundle.incrementCount();
			_sessions.put(session.getSessionID(), bundle);
		}
		
		return session;
	}
	
	public void releaseSession(Session session)
	{
		synchronized(_sessions)
		{
			SessionBundle bundle = _sessions.get(session.getSessionID());
			
			if (bundle != null)
				bundle.decrementCount();
		}
	}
	
	public void close()
	{
		synchronized(this)
		{
			if (_closed)
				return;
			
			_closed = true;
			_scavengerThread.interrupt();
		}
	}
	
	private class SessionScavenger implements Runnable
	{
		public void run()
		{
			SessionBundle []sessions;
			Collection<SessionBundle> scavengeList;
			
			while (!_closed)
			{
				try
				{
					Thread.sleep(_minIdleTime);
					synchronized(_sessions)
					{
						sessions = _sessions.values().toArray(new SessionBundle[0]);
					}
					
					scavengeList = new LinkedList<SessionBundle>();
					for (SessionBundle bundle : sessions)
					{
						if (bundle.isScavengeable(_minIdleTime))
						{
							scavengeList.add(bundle);
						}
					}
					
					if (scavengeList.size() > 0)
					{
						synchronized(_sessions)
						{
							for (SessionBundle bundle : scavengeList)
							{
								_sessions.remove(bundle.getSession().getSessionID());
							}
						}
					}
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
	}
}