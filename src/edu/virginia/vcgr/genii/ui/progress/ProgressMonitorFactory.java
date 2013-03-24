package edu.virginia.vcgr.genii.ui.progress;

import java.awt.Component;
import java.awt.Window;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.SwingUtilities;

public class ProgressMonitorFactory
{
	private ExecutorService _executor;

	public ProgressMonitorFactory(ExecutorService executor)
	{
		_executor = executor;
	}

	public <ResultType> ProgressMonitor createMonitor(Component parentComponent, String dialogTitle, String dialogSubTitle,
		long dialogDelay, Task<ResultType> task, TaskCompletionListener<ResultType> completionListener)
	{
		ProgressMonitorWorker<ResultType> worker = new ProgressMonitorWorker<ResultType>(
			SwingUtilities.getWindowAncestor(parentComponent), dialogTitle, dialogSubTitle, dialogDelay, task,
			completionListener);
		return worker;
	}

	private class ProgressMonitorWorker<ResultType> extends AbstractTaskProgressMonitor implements Runnable
	{
		private Window _owner;
		private String _title;
		private String _subTitle;
		private long _delay;
		private Task<ResultType> _task;
		private TaskCompletionListener<ResultType> _completionListener;
		private ResultType _result = null;

		private boolean initialWait(Future<ResultType> future, boolean finite) throws ExecutionException
		{
			try {
				if (finite)
					_result = future.get(_delay, TimeUnit.MILLISECONDS);
				else
					_result = future.get();
				return true;
			} catch (TimeoutException te) {
				return false;
			} catch (InterruptedException e) {
				return false;
			}
		}

		private ProgressMonitorWorker(Window owner, String title, String subTitle, long delay, Task<ResultType> task,
			TaskCompletionListener<ResultType> completionListener)
		{
			_owner = owner;
			_title = title;
			_subTitle = subTitle;
			_delay = delay;
			_task = task;
			_completionListener = completionListener;
		}

		@Override
		public void run()
		{
			fireTaskStarted();
			ProgressMonitorDialog dialog = null;
			ProgressListenerPipe pipe = new ProgressListenerPipe();

			try {
				Future<ResultType> future = _executor.submit(new TaskExecutorWorker<ResultType>(_task, pipe));

				if (!initialWait(future, _task.showProgressDialog())) {
					dialog = new ProgressMonitorDialog(_owner, _title, _subTitle);
					pipe.setListener(dialog.taskProgressListener());
					dialog.popup(_task, future);
					while (true) {
						try {
							_result = future.get();
							if (_completionListener != null)
								new TaskCompletedWorker<ResultType>(this, _completionListener, _task, _result).run();
							return;
						} catch (InterruptedException ie) {
							Thread.interrupted();
						}
					}
				} else {
					if (_completionListener != null)
						new TaskCompletedWorker<ResultType>(this, _completionListener, _task, _result).run();
				}
			} catch (ExecutionException ee) {
				if (_completionListener != null)
					new TaskExceptedWorker<ResultType>(this, _completionListener, _task, ee.getCause()).run();
			} catch (CancellationException cause) {
				if (_completionListener != null)
					new TaskCancelledWorker<ResultType>(this, _completionListener, _task).run();
			} finally {
				if (dialog != null) {
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		}

		@Override
		final public void start()
		{
			_executor.submit(this);
		}
	}

	private class TaskExecutorWorker<ResultType> implements Callable<ResultType>
	{
		private Task<ResultType> _task;
		private TaskProgressListener _progressListener;

		private TaskExecutorWorker(Task<ResultType> task, TaskProgressListener progressListener)
		{
			_task = task;
			_progressListener = progressListener;
		}

		@Override
		public ResultType call() throws Exception
		{
			return _task.execute(_progressListener);
		}
	}

	private class TaskCompletedWorker<ResultType> implements Runnable
	{
		private AbstractTaskProgressMonitor _monitor;
		private TaskCompletionListener<ResultType> _listener;
		private Task<ResultType> _task;
		private ResultType _result;

		private TaskCompletedWorker(AbstractTaskProgressMonitor monitor, TaskCompletionListener<ResultType> listener,
			Task<ResultType> task, ResultType result)
		{
			_monitor = monitor;
			_listener = listener;
			_task = task;
			_result = result;
		}

		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(this);
				return;
			}

			_listener.taskCompleted(_task, _result);
			_monitor.fireTaskCompleted();
		}
	}

	private class TaskCancelledWorker<ResultType> implements Runnable
	{
		private AbstractTaskProgressMonitor _monitor;
		private TaskCompletionListener<ResultType> _listener;
		private Task<ResultType> _task;

		private TaskCancelledWorker(AbstractTaskProgressMonitor monitor, TaskCompletionListener<ResultType> listener,
			Task<ResultType> task)
		{
			_monitor = monitor;
			_listener = listener;
			_task = task;
		}

		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(this);
				return;
			}

			_listener.taskCancelled(_task);
			_monitor.fireTaskCompleted();
		}
	}

	private class TaskExceptedWorker<ResultType> implements Runnable
	{
		private AbstractTaskProgressMonitor _monitor;
		private TaskCompletionListener<ResultType> _listener;
		private Task<ResultType> _task;
		private Throwable _exception;

		private TaskExceptedWorker(AbstractTaskProgressMonitor monitor, TaskCompletionListener<ResultType> listener,
			Task<ResultType> task, Throwable exception)
		{
			_monitor = monitor;
			_listener = listener;
			_task = task;
			_exception = exception;
		}

		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(this);
				return;
			}

			_listener.taskExcepted(_task, _exception);
			_monitor.fireTaskCompleted();
		}
	}

	private class ProgressListenerPipe implements TaskProgressListener
	{
		private TaskProgressListener _listener = null;
		private String _subTitle = null;

		synchronized private void setListener(TaskProgressListener listener)
		{
			_listener = listener;
			if (_listener != null && _subTitle != null)
				_listener.updateSubTitle(_subTitle);
		}

		@Override
		synchronized public void updateSubTitle(String subTitle)
		{
			if (_listener != null)
				_listener.updateSubTitle(subTitle);

			_subTitle = subTitle;
		}
	}
}