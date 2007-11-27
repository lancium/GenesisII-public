package org.morgan.ftp;

import java.util.ArrayList;

public class FTPListenerManager
{
	private ArrayList<FTPListener> _listeners = new ArrayList<FTPListener>();
	
	public void addFTPListener(FTPListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public void removeFTPListener(FTPListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	public void fireSessionOpened(int sessionID)
	{
		FTPListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = _listeners.toArray(new FTPListener[0]);
		}
		
		for (FTPListener listener : listeners)
		{
			listener.sessionOpened(sessionID);
		}
	}
	
	public void fireSessionClosed(int sessionID)
	{
		FTPListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = _listeners.toArray(new FTPListener[0]);
		}
		
		for (FTPListener listener : listeners)
		{
			listener.sessionClosed(sessionID);
		}
	}
	
	public void fireUserAuthenticated(int sessionID, String username)
	{
		FTPListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = _listeners.toArray(new FTPListener[0]);
		}
		
		for (FTPListener listener : listeners)
		{
			listener.userAuthenticated(sessionID, username);
		}
	}
}