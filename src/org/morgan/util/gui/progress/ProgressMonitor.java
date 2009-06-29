package org.morgan.util.gui.progress;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ProgressMonitor<Type>
{
	static final private ExecutorService _executors =
		Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r)
			{
				Thread th = new Thread(r, "Progress Thread");
				th.setDaemon(true);
				return th;
			}
		});
	
	private Collection<ProgressListener<Type>> _listeners =
		new LinkedList<ProgressListener<Type>>();
	private Collection<ProgressNotifier> _notifiers =
		new LinkedList<ProgressNotifier>();
	
	final public ProgressListener<Type> addProgressListener(
		ProgressListener<Type> listener,
		boolean mustHappenOnEventDispatchThread)
	{
		ProgressListener<Type> ret = mustHappenOnEventDispatchThread ?
			new SwingInvokeProgressListenerHolder<Type>(listener) :
			listener;
			
		synchronized(_listeners)
		{
			_listeners.add(ret);
		}
		
		return ret;
	}
	
	final public ProgressNotifier addProgressNotifier(
		ProgressNotifier notifier, boolean notifyOnEventDispatchThread)
	{
		ProgressNotifier ret = notifyOnEventDispatchThread ?
			new SwingInvokeProgressNotifierHolder(notifier) :
			notifier;
			
		synchronized(_notifiers)
		{
			_notifiers.add(ret);
		}
		
		return ret;
	}
	
	final public void removeProgressListener(ProgressListener<Type> listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	final public void removeProgressNotifier(ProgressNotifier notifier)
	{
		synchronized(_notifiers)
		{
			_notifiers.remove(notifier);
		}
	}

	final public void startTask(ProgressTask<Type> task)
	{
		Collection<ProgressListener<Type>> listeners;
		Collection<ProgressNotifier> notifiers;
		
		synchronized(_listeners)
		{
			listeners = new LinkedList<ProgressListener<Type>>(_listeners);
		}
		
		synchronized(_notifiers)
		{
			notifiers = new LinkedList<ProgressNotifier>(_notifiers);
		}
		
		ProgressWatcher<Type> watcher = new ProgressWatcher<Type>(
			_executors, task, listeners, notifiers);
		_executors.execute(watcher);
	}
}