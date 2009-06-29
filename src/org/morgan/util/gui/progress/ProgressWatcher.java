package org.morgan.util.gui.progress;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ProgressWatcher<Type> implements Runnable
{
	private ExecutorService _executor;
	private ProgressTask<Type> _task;
	private Collection<ProgressListener<Type>> _listeners;
	private ProgressNotifierMultiplexer _notifier;
	
	private boolean _cancelled = false;
	private Future<Type> _future = null;
	
	protected void fireTaskCancelled()
	{
		for (ProgressListener<Type> listener : _listeners)
			listener.taskCancelled();
		_notifier.finished();
	}
	
	protected void fireTaskExcepted(Exception e)
	{
		for (ProgressListener<Type> listener : _listeners)
			listener.taskExcepted(e);
		_notifier.finished();
	}
	
	protected void fireTaskCompleted(Type result)
	{
		for (ProgressListener<Type> listener : _listeners)
			listener.taskCompleted(result);
		_notifier.finished();
	}
	
	ProgressWatcher(ExecutorService executor,
		ProgressTask<Type> task,
		Collection<ProgressListener<Type>> listeners,
		Collection<ProgressNotifier> notifiers)
	{
		_executor = executor;
		_task = task;
		_listeners = listeners;
		_notifier = new ProgressNotifierMultiplexer(notifiers);
	}
	
	@Override
	public void run()
	{
		CancelController controller = new CancelControllerImpl();
		CallableProgressTask<Type> callable = new CallableProgressTask<Type>(
			_task, _notifier);
		
		_notifier.initialize(controller, _task);
		
		synchronized(this)
		{
			if (_cancelled)
				fireTaskCancelled();
			_future = _executor.submit(callable);
		}
		
		try
		{
			Type result = _future.get();
			if (_future.isCancelled())
				fireTaskCancelled();
			else
				fireTaskCompleted(result);
		}
		catch (InterruptedException e)
		{
			Thread.interrupted();
			if (_future.isCancelled())
				fireTaskCancelled();
			else
				fireTaskExcepted(e);
		}
		catch (Exception e)
		{
			if (_future.isCancelled())
				fireTaskCancelled();
			else
				fireTaskExcepted(e);
		}
	}
	
	private class CancelControllerImpl implements CancelController
	{
		@Override
		public void cancelRequested()
		{
			synchronized(ProgressWatcher.this)
			{
				_cancelled = true;
				if (_future != null)
					_future.cancel(true);
			}
		}
	}
}