package edu.virginia.vcgr.genii.container.bes;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.machine.MachineFacetUpdater;
import edu.virginia.vcgr.genii.client.machine.MachineListener;

public class BESPolicyEnactor implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESPolicyEnactor.class);
	
	static private final long USER_LOGIN_CHECK_FREQUENCY 
		= 1000 * 30;	/* 30 seconds */
	static private final long SCREENSAVER_INACTIVE_CHECK_FREQUENCY 
		= 1000 * 30;	/* 30 seconds */
	
	static private MachineFacetUpdater _updater;
	
	static
	{
		_updater = new MachineFacetUpdater(USER_LOGIN_CHECK_FREQUENCY, 
			SCREENSAVER_INACTIVE_CHECK_FREQUENCY);
		_updater.start();
	}
	
	private BESPolicy _policy;
	private Boolean _lastUserLoggedInStatus = null;
	private Boolean _lastScreenSaverActiveStatus = null;
	private BESPolicyActions _lastAction;
	private BESPolicyMachineListener _machineListener = null;
	private Collection<BESPolicyListener> _listeners = new
		ArrayList<BESPolicyListener>();
	
	public BESPolicyEnactor(BESPolicy policy)
	{
		_policy = policy;
		_machineListener = new BESPolicyMachineListener();
		_lastAction = _policy.getCurrentAction(_lastUserLoggedInStatus, 
			_lastScreenSaverActiveStatus);
		
		_updater.addMachineListener(_machineListener);
	}
	
	protected void finalize()
	{
		close();
	}
	
	synchronized public void close()
	{
		if (_machineListener != null)
			_updater.removeMachineListener(_machineListener);
		
		_machineListener = null;
	}
	
	public void setPolicy(BESPolicy policy)
	{
		_policy = policy;
		updateAction();
	}
	
	public void addBESPolicyListener(BESPolicyListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public void removeBESPolicyListener(BESPolicyListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	private void fireNewAction(BESPolicyActions action)
	{
		Collection<BESPolicyListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<BESPolicyListener>(_listeners);
		}
		
		for (BESPolicyListener listener : listeners)
		{
			try
			{
				if (action.equals(BESPolicyActions.NOACTION))
					listener.resume();
				else if (action.equals(BESPolicyActions.SUSPEND))
					listener.suspend();
				else if (action.equals(BESPolicyActions.SUSPENDORKILL))
					listener.suspendOrKill();
				else if (action.equals(BESPolicyActions.KILL))
					listener.kill();
			}
			catch (Throwable cause)
			{
				_logger.error("Exception thrown while enacting BES policy.", cause);
			}
		}
	}
	
	private void updateAction()
	{
		BESPolicyActions newAction = _policy.getCurrentAction(
			_lastUserLoggedInStatus, _lastScreenSaverActiveStatus);
		if (!newAction.equals(_lastAction))
		{
			_lastAction = newAction;
			fireNewAction(newAction);
		}
	}
	
	synchronized BESPolicyActions getCurrentAction()
	{
		return _lastAction;
	}
	
	private class BESPolicyMachineListener implements MachineListener
	{
		@Override
		public void screenSaverActivated(boolean activated)
		{
			_lastScreenSaverActiveStatus = new Boolean(activated);
		}

		@Override
		public void userLoggedIn(boolean loggedIn)
		{
			_lastUserLoggedInStatus = new Boolean(loggedIn);
		}
	}
}