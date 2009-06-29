package org.morgan.util.gui.progress;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

class SwingInvokeProgressNotifierHolder implements ProgressNotifier
{
	private ProgressNotifier _notifier;

	public SwingInvokeProgressNotifierHolder(ProgressNotifier notifier)
	{
		if (notifier == null)
			throw new IllegalArgumentException("Notifier cannot be null.");
		
		_notifier = notifier;
	}
	
	@Override
	public void initialize(CancelController controller,
		ProgressTask<?> task)
	{
		try
		{
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(
					new Initializer(controller, task));
			else
				_notifier.initialize(controller, task);
		}
		catch (InvocationTargetException ite)
		{
			System.err.println("Unable to initialize notifier.");
		} 
		catch (InterruptedException e)
		{
			System.err.println("Unable to initialize notifier.");
		}
	}
	
	@Override
	public void updateNote(String newNote)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new NoteUpdater(newNote));
		else
			_notifier.updateNote(newNote);
	}

	@Override
	public void updateProgress(int newValue)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new ProgressUpdater(newValue));
		else
			_notifier.updateProgress(newValue);
	}
	
	@Override
	public void finished()
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new Finisher());
		else
			_notifier.finished();
	}
	
	private class Initializer implements Runnable
	{
		private CancelController _controller;
		private ProgressTask<?> _task;
		
		private Initializer(CancelController controller,
			ProgressTask<?> task)
		{
			_controller = controller;
			_task = task;
		}
		
		@Override
		public void run()
		{
			_notifier.initialize(_controller, _task);
		}
	}
	
	private class NoteUpdater implements Runnable
	{
		private String _note;
		
		private NoteUpdater(String note)
		{
			_note = note;
		}
		
		@Override
		public void run()
		{
			_notifier.updateNote(_note);
		}
	}
	
	private class ProgressUpdater implements Runnable
	{
		private int _progress;
		
		private ProgressUpdater(int progress)
		{
			_progress = progress;
		}
		
		@Override
		public void run()
		{
			_notifier.updateProgress(_progress);
		}
	}
	
	private class Finisher implements Runnable
	{
		@Override
		public void run()
		{
			_notifier.finished();
		}
	}
}
