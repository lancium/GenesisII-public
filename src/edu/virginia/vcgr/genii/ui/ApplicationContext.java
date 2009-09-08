package edu.virginia.vcgr.genii.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.container.ContainerIDFile;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class ApplicationContext
{
	private Collection<QuitListener> _quitListeners = 
		new LinkedList<QuitListener>();
	private Collection<DisposeListener> _disposeListeners =
		new LinkedList<DisposeListener>();
	private GUID _localContainerID;
	
	private ApplicationEventListener _applicationEventListener = null;
	
	ApplicationContext()
	{
		_localContainerID = ContainerIDFile.containerID();
	}
	
	public void addQuitListener(QuitListener listener)
	{
		synchronized(_quitListeners)
		{
			_quitListeners.add(listener);
		}
	}
	
	public void removeQuitListener(QuitListener listener)
	{
		synchronized(_quitListeners)
		{
			_quitListeners.remove(listener);
		}
	}
	
	public boolean fireQuitRequested()
	{
		QuitListener []listeners;
		
		synchronized(_quitListeners)
		{
			listeners = _quitListeners.toArray(
				new QuitListener[_quitListeners.size()]);
		}
		
		for (QuitListener listener : listeners)
		{
			if (!listener.quitRequested())
				return false;
		}
		
		return true;
	}
	
	public void addDisposeListener(DisposeListener listener)
	{
		synchronized(_disposeListeners)
		{
			_disposeListeners.add(listener);
		}
	}
	
	public void removeDisposeListener(DisposeListener listener)
	{
		synchronized(_disposeListeners)
		{
			_disposeListeners.remove(listener);
		}
	}
	
	public void fireDispose()
	{
		DisposeListener []listeners;
		
		synchronized(_disposeListeners)
		{
			listeners = _disposeListeners.toArray(
				new DisposeListener[_disposeListeners.size()]);
		}
		
		for (DisposeListener listener : listeners)
			listener.dispose();
	}
	
	public void setApplicationEventListener(ApplicationEventListener listener)
	{
		synchronized(this)
		{
			if (_applicationEventListener != null)
				throw new IllegalStateException(
					"Application Event Listener already set.");
			_applicationEventListener = listener;
		}
	}
	
	public boolean fireApplicationQuitRequested()
	{
		synchronized(this)
		{
			if (_applicationEventListener == null)
				throw new IllegalStateException(
					"Application Event Listener not set.");
		}
		
		return _applicationEventListener.quitRequested();
	}
	
	public void fireApplicationPreferencesRequested()
	{
		synchronized(this)
		{
			if (_applicationEventListener == null)
				throw new IllegalStateException(
					"Application Event Listener not set.");
		}
		
		_applicationEventListener.preferencesRequested();
	}
	
	public void fireApplicationAboutRequested()
	{
		synchronized(this)
		{
			if (_applicationEventListener == null)
				throw new IllegalStateException(
					"Application Event Listener not set.");
		}
		
		_applicationEventListener.aboutRequested();
	}
	
	public boolean isMacOS()
	{
		return OperatingSystemType.getCurrent() == OperatingSystemType.MACOS;
	}
	
	public boolean isLocal(EndpointReferenceType epr)
	{
		if (_localContainerID == null)
			return false;
		
		GUID eprContainerID = EPRUtils.extractContainerID(epr);
		if (eprContainerID == null)
			return false;
		
		return _localContainerID.equals(eprContainerID);
	}
}