package org.morgan.util.gui.progress.test;

import org.morgan.util.gui.progress.ProgressNotifier;
import org.morgan.util.gui.progress.ProgressTask;

class TestTask implements ProgressTask<String>
{
	private long _sleepDuration;
	private boolean _determinate;
	private String []_notes;
	
	TestTask(long sleepDuration, boolean determinate, String...notes)
	{
		_sleepDuration = sleepDuration;
		_determinate = determinate;
		_notes = notes;
	}
	
	@Override
	public String compute(ProgressNotifier notifier) throws Exception
	{
		int lcv = 0;
		for (lcv = 0; lcv < _notes.length; lcv++)
		{
			notifier.updateNote(_notes[lcv]);
			Thread.sleep(_sleepDuration);
			if (_determinate)
				notifier.updateProgress(lcv + 1);
		}
		
		return "Hello, World!";
	}

	@Override
	public int getMaximumProgressValue()
	{
		return _notes.length;
	}

	@Override
	public int getMinimumProgressValue()
	{
		return 0;
	}

	@Override
	public boolean isProgressIndeterminate()
	{
		return !_determinate;
	}
}