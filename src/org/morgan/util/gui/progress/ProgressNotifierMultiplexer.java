package org.morgan.util.gui.progress;

import java.util.Collection;

class ProgressNotifierMultiplexer implements ProgressNotifier
{
	private Collection<ProgressNotifier> _notifiers;
	
	ProgressNotifierMultiplexer(Collection<ProgressNotifier> notifiers)
	{
		_notifiers = notifiers;
	}
	
	@Override
	public void initialize(CancelController cancelController, 
		ProgressTask<?> task)
	{
		for (ProgressNotifier notifier : _notifiers)
			notifier.initialize(cancelController, task);
	}

	@Override
	public void updateNote(String newNote)
	{
		for (ProgressNotifier notifier : _notifiers)
			notifier.updateNote(newNote);
	}

	@Override
	public void updateProgress(int newValue)
	{
		for (ProgressNotifier notifier : _notifiers)
			notifier.updateProgress(newValue);
	}
	
	@Override
	public void finished()
	{
		for (ProgressNotifier notifier : _notifiers)
			notifier.finished();
	}
}