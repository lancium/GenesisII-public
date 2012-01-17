package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.ui.UIContext;

public abstract class LoginPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private Collection<LoginPanelListener> _listeners =
		new LinkedList<LoginPanelListener>();
	
	protected void fireLoginInformationValid(boolean isValid)
	{
		Collection<LoginPanelListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<LoginPanelListener>(_listeners);
		}
		
		for (LoginPanelListener listener : listeners)
		{
			listener.loginInformationValid(isValid);
		}
	}
	
	protected LoginPanel()
	{
		super(new GridBagLayout());
	}
	
	public abstract Collection<GIICredential> doLogin(UIContext uiContext) 
		throws Throwable;
	public abstract boolean isLoginInformationValid();
	
	final public void addLoginPanelListener(LoginPanelListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
		
		listener.loginInformationValid(isLoginInformationValid());
	}
	
	final public void removeLoginPanelListener(LoginPanelListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
}