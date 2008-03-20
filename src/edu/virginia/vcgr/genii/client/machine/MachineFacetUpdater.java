package edu.virginia.vcgr.genii.client.machine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class MachineFacetUpdater
{
	private long _userLoggedInCheckFrequency;
	private long _screenSaverActiveCheckFrequency;
	
	private Object _userLoggedInLock = new Object();
	private Object _screenSaverActiveLock = new Object();
	
	private Boolean _lastUserLoggedInState = null;
	private Boolean _lastScreenSaverActiveState = null;
	
	private MachineInterrogator _interrogator;
	
	private Collection<MachineListener> _listeners =
		new LinkedList<MachineListener>();
	
	private Thread _updater = null;
	
	public MachineFacetUpdater(long userLoggedInCheckFrequency,
		long screenSaverActiveCheckFrequency)
	{
		_userLoggedInCheckFrequency = userLoggedInCheckFrequency;
		_screenSaverActiveCheckFrequency = screenSaverActiveCheckFrequency;
		
		_interrogator = MachineFactory.getInterrogatorInstance();
		
		if (_interrogator.canDetermineUserLoggedIn())
			_lastUserLoggedInState = new Boolean(_interrogator.isUserLoggedIn());
		if (_interrogator.canDetermineScreenSaverActive())
			_lastScreenSaverActiveState = new Boolean(
				_interrogator.isScreenSaverActive());
	}
	
	protected void finalize()
	{
		stop();
	}
	
	synchronized public void start()
	{
		if (_updater == null)
		{
			_updater = new Thread(
				new AutoUpdater(), "Machine State Auto Updater.");
			_updater.setDaemon(true);
			_updater.start();
		}
	}
	
	synchronized public void stop()
	{
		if (_updater != null)
		{
			_updater = null;
			_updater.interrupt();
		}
		
		_updater = null;
	}
	
	public void addMachineListener(MachineListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
		
		boolean doFire = false;
		boolean fireValue = false;
		
		synchronized(_userLoggedInLock)
		{
			if (_lastUserLoggedInState != null)
			{
				doFire = true;
				fireValue = _lastUserLoggedInState.booleanValue();
			}
		}
		
		if (doFire)
			listener.userLoggedIn(fireValue);
		
		doFire = false;
		fireValue = false;
		
		synchronized(_screenSaverActiveLock)
		{
			if (_lastScreenSaverActiveState != null)
			{
				doFire = true;
				fireValue = _lastScreenSaverActiveState.booleanValue();
			}
		}
		
		if (doFire)
			listener.screenSaverActivated(fireValue);
	}
	
	public void removeMachineListener(MachineListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	private void fireUserLoggedIn(boolean loggedIn)
	{
		Collection<MachineListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<MachineListener>(_listeners);
		}
		
		for (MachineListener listener : listeners)
		{
			listener.userLoggedIn(loggedIn);
		}
	}
	
	private void fireScreenSaverActived(boolean screenSaverActivated)
	{
		Collection<MachineListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<MachineListener>(_listeners);
		}
		
		for (MachineListener listener : listeners)
		{
			listener.screenSaverActivated(screenSaverActivated);
		}
	}
	
	public void updateUserLoggedInState()
	{
		boolean doFire = false;
		boolean fireValue = false;
		
		synchronized(_userLoggedInLock)
		{
			if (_lastUserLoggedInState == null)
				return;
			
			boolean lastState = _lastUserLoggedInState.booleanValue();
			_lastUserLoggedInState = new Boolean(_interrogator.isUserLoggedIn());
			
			if (lastState != _lastUserLoggedInState.booleanValue())
			{
				doFire = true;
				fireValue = _lastUserLoggedInState.booleanValue();
			}
		}
		
		if (doFire)
			fireUserLoggedIn(fireValue);
	}
	
	public void updateScreenSaverActiveState()
	{
		boolean doFire = false;
		boolean fireValue = false;
		
		synchronized(_screenSaverActiveLock)
		{
			if (_lastScreenSaverActiveState == null)
				return;
			
			boolean lastState = _lastScreenSaverActiveState.booleanValue();
			_lastScreenSaverActiveState = new Boolean(_interrogator.isScreenSaverActive());
			
			if (lastState != _lastScreenSaverActiveState.booleanValue())
			{
				doFire = true;
				fireValue = _lastScreenSaverActiveState.booleanValue();
			}
		}
		
		if (doFire)
			fireScreenSaverActived(fireValue);
	}
	
	public void update()
	{
		updateScreenSaverActiveState();
		updateUserLoggedInState();
	}
	
	private class AutoUpdater implements Runnable
	{
		private long _nextUserLoggedInUpdate;
		private long _nextScreenSaverActiveUpdate;
		
		public void run()
		{
			long currentTime;
			
			boolean userLoggedInChecked = false;
			boolean screenSaverActiveChecked = false;
			
			_nextUserLoggedInUpdate = 0;
			_nextScreenSaverActiveUpdate = 0;
			
			while (true)
			{
				if (_updater == null)
					break;
				
				userLoggedInChecked = false;
				screenSaverActiveChecked = false;
				currentTime = System.currentTimeMillis();
				if (currentTime >= _nextUserLoggedInUpdate)
				{
					updateUserLoggedInState(); 
					userLoggedInChecked = true;
				}
				if (currentTime >= _nextScreenSaverActiveUpdate)
				{
					updateScreenSaverActiveState();
					screenSaverActiveChecked = true;
				}
				
				if (_updater == null)
					break;
				
				currentTime = System.currentTimeMillis();
				if (userLoggedInChecked)
					_nextUserLoggedInUpdate = 
						currentTime + _userLoggedInCheckFrequency;
				if (screenSaverActiveChecked)
					_nextScreenSaverActiveUpdate =
						currentTime + _screenSaverActiveCheckFrequency;
				
				long nextUpdate = Math.min(_nextUserLoggedInUpdate, 
					_nextScreenSaverActiveUpdate);
				
				long waitTime = (nextUpdate - currentTime);
				if (waitTime > 0)
				{
					try
					{
						Thread.sleep(waitTime);
					}
					catch (InterruptedException ie)
					{
						Thread.currentThread().isInterrupted();
					}
				}
			}
		}
	}
}