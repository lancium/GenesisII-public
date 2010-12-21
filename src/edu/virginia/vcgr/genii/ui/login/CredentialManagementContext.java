package edu.virginia.vcgr.genii.ui.login;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;

final public class CredentialManagementContext
{
	private UIContext _uiContext;
	
	private Collection<CredentialListener> _listeners =
		new LinkedList<CredentialListener>();
	
	private LoginAction _loginAction;
	private LogoutAction _logoutAction;
	private LogoutAllAction _logoutAllAction;
	
	final protected void fireCredentialsChanged()
	{
		Collection<CredentialListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<CredentialListener>(_listeners);
		}
		
		for (CredentialListener listener : listeners)
			listener.credentialsChanged(this);
	}
	
	CredentialManagementContext(UIContext uiContext)
	{
		_uiContext = uiContext;
		
		_loginAction = new LoginAction();
		_logoutAction = new LogoutAction();
		_logoutAllAction = new LogoutAllAction();
		
		addCredentialListener(_loginAction);
		addCredentialListener(_logoutAction);
		addCredentialListener(_logoutAllAction);
	}
	
	final public Collection<GIICredential> loginItems()
	{
		TransientCredentials transientCredentials = 
			TransientCredentials.getTransientCredentials(
				_uiContext.callingContext());
		return transientCredentials._credentials;
	}
	
	final public void logoutAll(Component source)
	{
		int response = JOptionPane.showConfirmDialog(
			source, 
			"Are you sure you want to log out from all credentials?", 
			"Logout All Confirmation", JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				TransientCredentials.globalLogout(_uiContext.callingContext());
				_uiContext.callingContext().setActiveKeyAndCertMaterial(null);
				fireCredentialsChanged();
			}
			catch (Throwable cause)
			{
				ErrorHandler.handleError(_uiContext,
					(JComponent)source, cause);
			}
		}
	}
	
	final public void logout(Component source)
	{
		new LogoutDialog(source, this);
	}
	
	final public void logout(Component source, GIICredential []credentials)
	{
		TransientCredentials transientCredentials = 
			TransientCredentials.getTransientCredentials(
				_uiContext.callingContext());
		for (GIICredential credential : credentials)
			transientCredentials._credentials.remove(credential);
		fireCredentialsChanged();
	}
	
	final public void login(Component source)
	{
		Collection<GIICredential> credentials = LoginDialog.doLogin(
			source, _uiContext);
		if (credentials != null)
		{
			TransientCredentials.getTransientCredentials(
				_uiContext.callingContext())._credentials.addAll(credentials);
			fireCredentialsChanged();
		}
	}
	
	final public void addCredentialListener(CredentialListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	final public void removeCredentialListener(CredentialListener listener)
	{
		synchronized (_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	final public Action loginAction()
	{
		return _loginAction;
	}
	
	final public Action logoutAction()
	{
		return _logoutAction;
	}
	
	final public Action logoutAllAction()
	{
		return _logoutAllAction;
	}

	private class LoginAction extends AbstractAction
		implements CredentialListener
	{
		static final long serialVersionUID = 0L;
		
		private LoginAction()
		{
			super("Login");
			
			evaluateStatus();
		}
		
		final private void evaluateStatus()
		{
			// Nothing to do
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			login((Component)e.getSource());
		}

		@Override
		final public void credentialsChanged(
			CredentialManagementContext context)
		{
			evaluateStatus();
		}
	}
	
	private class LogoutAction extends AbstractAction 
		implements CredentialListener
	{
		static final long serialVersionUID = 0L;
		
		private LogoutAction()
		{
			super("Logout");
			
			evaluateStatus();
		}
		
		final private void evaluateStatus()
		{
			setEnabled(!loginItems().isEmpty());
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			logout((Component)e.getSource());
		}
		
		@Override
		final public void credentialsChanged(
			CredentialManagementContext context)
		{
			evaluateStatus();
		}
	}
	
	private class LogoutAllAction extends AbstractAction
		implements CredentialListener
	{
		static final long serialVersionUID = 0L;
		
		private LogoutAllAction()
		{
			super("Logout All");

			evaluateStatus();
		}
		
		final private void evaluateStatus()
		{
			setEnabled(!loginItems().isEmpty());
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			logoutAll((Component)e.getSource());
		}

		@Override
		final public void credentialsChanged(
			CredentialManagementContext context)
		{
			evaluateStatus();
		}
	}
}