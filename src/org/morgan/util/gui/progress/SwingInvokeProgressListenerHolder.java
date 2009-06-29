package org.morgan.util.gui.progress;

import javax.swing.SwingUtilities;

class SwingInvokeProgressListenerHolder<Type> implements ProgressListener<Type>
{
	private ProgressListener<Type> _listener;
	
	public SwingInvokeProgressListenerHolder(ProgressListener<Type> listener)
	{
		if (listener == null)
			throw new IllegalArgumentException("Listener cannot be null.");
		
		_listener = listener;
	}
	
	@Override
	public void taskCancelled()
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new Canceller());
		else
			_listener.taskCancelled();
	}

	@Override
	public void taskCompleted(Type result)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new Completer(result));
		else
			_listener.taskCompleted(result);
	}

	@Override
	public void taskExcepted(Exception e)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new Exceptor(e));
		else
			_listener.taskExcepted(e);
	}
	
	private class Canceller implements Runnable
	{
		@Override
		public void run()
		{
			_listener.taskCancelled();
		}
	}
	
	private class Completer implements Runnable
	{
		private Type _result;
		
		private Completer(Type result)
		{
			_result = result;
		}
		
		@Override
		public void run()
		{
			_listener.taskCompleted(_result);
		}
	}
	
	private class Exceptor implements Runnable
	{
		private Exception _e;
		
		private Exceptor(Exception e)
		{
			_e = e;
		}
		
		@Override
		public void run()
		{
			_listener.taskExcepted(_e);
		}
	}
}