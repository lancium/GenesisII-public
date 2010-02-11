package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import edu.virginia.vcgr.genii.client.utils.Duration;

/**
 * An Information Portal is a portal through which "information" can be 
 * acquired about endpoints.  This portal can cache values and gets updated
 * values on separate threads from a thread pool.
 * 
 * @author mmm2a
 *
 * @param <InformationType>
 */
public class InformationPortal<InformationType> implements Closeable
{
	private Duration _defaultTimeout;
	private Duration _defaultCacheWindow;
	
	private Executor _executor;
	private boolean _closed = false;
	
	private InformationPersister<InformationType> _persister;
	private InformationResolver<InformationType> _resolver;
	
	private Set<InformationEndpoint> _currentlyResolving =
		new HashSet<InformationEndpoint>();
	
	private Map<InformationEndpoint, Collection<WaitingListener<InformationType>>> _waitingListeners =
		new HashMap<InformationEndpoint, Collection<WaitingListener<InformationType>>>();
	
	private void handleTimeout(InformationEndpoint endpoint,
		InformationListener<InformationType> listener)
	{
		InformationResult<InformationType> oldResult;
		InformationResult<InformationType> newResult;
		
		synchronized(_persister)
		{
			oldResult = _persister.get(endpoint);
			if (oldResult != null)
				newResult = new InformationResult<InformationType>(
					oldResult.information(), Calendar.getInstance(),
					new TimeoutException());
			else
				newResult = new InformationResult<InformationType>(
					null, Calendar.getInstance(), new TimeoutException());
			_persister.persist(endpoint, newResult);
		}
		
		listener.informationUpdated(endpoint, newResult);
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	InformationPortal(Executor executor,
		InformationPersister<InformationType> persister,
		InformationResolver<InformationType> resolver,
		Duration defaultTimeout, Duration defaultCacheWindow)
	{
		_executor = executor;
		_persister = persister;
		_resolver = resolver;
		
		_defaultTimeout = defaultTimeout;
		_defaultCacheWindow = defaultCacheWindow;
		
		Thread th = new Thread(new TimeoutWorker(), 
			"Information Portal Timeout Worker");
		th.setDaemon(true);
		th.start();
	}
	
	public void getInformation(InformationEndpoint endpoint,
		InformationListener<InformationType> listener,
		Duration timeout, Duration cacheWindow)
	{
		if (timeout == null)
			timeout = _defaultTimeout;
		if (cacheWindow == null)
			cacheWindow = _defaultCacheWindow;
		
		Calendar timeoutExpiration = timeout.getExpiration();
		
		WaitingListener<InformationType> waiter =
			new WaitingListener<InformationType>(listener, timeoutExpiration);
		
		synchronized(_waitingListeners)
		{
			synchronized(_persister)
			{
				InformationResult<InformationType> result =
					_persister.get(endpoint);
				if (result != null &&
					cacheWindow.getExpiration(
						result.lastUpdated()).after(Calendar.getInstance()))
				{
					listener.informationUpdated(endpoint, result);
					return;
				}
			}
			
			Collection<WaitingListener<InformationType>> listeners =
				_waitingListeners.get(endpoint);
			if (listeners == null)
				_waitingListeners.put(endpoint,
					listeners = new LinkedList<WaitingListener<InformationType>>());
			listeners.add(waiter);
			
			_waitingListeners.notifyAll();
		}
		
		synchronized(_currentlyResolving)
		{
			if (_currentlyResolving.contains(endpoint))
				return;
			
			_currentlyResolving.add(endpoint);
		}
		
		_executor.execute(new ResolverWorker(endpoint));
	}
	
	public void getInformation(InformationEndpoint endpoint,
		InformationListener<InformationType> listener)
	{
		getInformation(endpoint, listener, _defaultTimeout,
			_defaultCacheWindow);
	}
	
	public void getInformation(InformationEndpoint endpoint,
		InformationListener<InformationType> listener, boolean force)
	{
		if (!force)
			getInformation(endpoint, listener, _defaultTimeout,
				_defaultCacheWindow);
		else
			getInformation(endpoint, listener, _defaultTimeout,
				new Duration(1));
	}
	
	public InformationResult<InformationType> getInformation(
		InformationEndpoint endpoint,
		Duration timeout, Duration cacheWindow) throws InterruptedException
	{
		BlockingInformationListener<InformationType> listener =
			new BlockingInformationListener<InformationType>();
		getInformation(endpoint, listener, timeout, cacheWindow);
		return listener.get();
	}
	
	public InformationResult<InformationType> getInformation(
		InformationEndpoint endpoint) throws InterruptedException
	{
		return getInformation(endpoint,
			_defaultTimeout, _defaultCacheWindow);
	}
	
	@Override
	public void close() throws IOException
	{
		synchronized(_waitingListeners)
		{
			if (!_closed)
			{
				_closed = true;
				_waitingListeners.notifyAll();
			}
		}
	}	
	
	private class TimeoutWorker implements Runnable
	{
		private Calendar doTimeouts()
		{
			Calendar now = Calendar.getInstance();
			Calendar ret = null;
			
			for (InformationEndpoint endpoint : _waitingListeners.keySet())
			{
				Collection<WaitingListener<InformationType>> listeners =
					_waitingListeners.get(endpoint);
				if (listeners != null)
				{
					for (WaitingListener<InformationType> listener : listeners)
					{
						Calendar tmp = listener.getTimeout();
						if (tmp != null)
						{
							if (tmp.before(now))
							{
								listeners.remove(listener);
								handleTimeout(endpoint, listener.getListener());
							} else
							{
								if (ret == null)
									ret = tmp;
								else
								{
									if (tmp.before(ret))
										ret = tmp;
								}
							}
						}
					}
				}
			}
			
			return ret;
		}
		
		@Override
		public void run()
		{
			synchronized(_waitingListeners)
			{
				while (!_closed)
				{
					Calendar nextTimeout = doTimeouts();
					long timeout;
					if (nextTimeout == null)
						timeout = Long.MAX_VALUE;
					else
						timeout = nextTimeout.getTimeInMillis() - 
							System.currentTimeMillis();
					if (timeout < 0L);
					timeout = 0L;
					
					try
					{
						_waitingListeners.wait(timeout);
					}
					catch (InterruptedException ie)
					{
						Thread.interrupted();
					}
				}
			}
		}
	}
	
	private class ResolverWorker implements Runnable
	{
		private InformationEndpoint _endpoint;
		
		public ResolverWorker(InformationEndpoint endpoint)
		{
			_endpoint = endpoint;
		}
		
		@Override
		public void run()
		{
			InformationResult<InformationType> result = null;
			InformationType info = null;
			Throwable exception = null;
			
			try
			{
				info = _resolver.acquire(_endpoint, _defaultTimeout);
				result = new InformationResult<InformationType>(
					info, Calendar.getInstance(), null);
			}
			catch (Throwable cause)
			{
				exception = cause;
			}
			
			synchronized(_waitingListeners)
			{
				synchronized (_persister)
				{
					if (exception != null)
					{
						InformationResult<InformationType> oldResult =
							_persister.get(_endpoint);
						if (oldResult != null)
							result = new InformationResult<InformationType>(
								oldResult.information(), Calendar.getInstance(),
								exception);
						else
							result = new InformationResult<InformationType>(
								null, Calendar.getInstance(), exception);
					}
					
					_persister.persist(_endpoint, result);
				}
				
				Collection<WaitingListener<InformationType>> listeners =
					_waitingListeners.remove(_endpoint);
				for (WaitingListener<InformationType> listener : listeners)
					listener.getListener().informationUpdated(_endpoint, result);
			}
			
			synchronized(_currentlyResolving)
			{
				_currentlyResolving.remove(_endpoint);
			}
		}
	}
}