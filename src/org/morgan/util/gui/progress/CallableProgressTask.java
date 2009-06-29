package org.morgan.util.gui.progress;

import java.util.concurrent.Callable;

class CallableProgressTask<Type> implements Callable<Type>
{
	private ProgressNotifier _notifier;
	private ProgressTask<Type> _task;
	
	CallableProgressTask(ProgressTask<Type> task, 
		ProgressNotifier notifier)
	{
		_notifier = notifier;
		_task = task;
	}
	
	@Override
	public Type call() throws Exception
	{
		return _task.compute(_notifier);
	}
}