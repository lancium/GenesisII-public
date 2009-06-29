package org.morgan.util.gui.progress;

public abstract class AbstractProgressTask<Type> implements ProgressTask<Type>
{
	private Integer _minimumValue = null;
	private Integer _maximumValue = null;
	
	protected AbstractProgressTask()
	{
	}
	
	protected AbstractProgressTask(int minimumProgressValue, 
		int maximumProgressValue)
	{
		_minimumValue = new Integer(
			Math.min(minimumProgressValue, maximumProgressValue));
		_maximumValue = new Integer(
			Math.max(minimumProgressValue, maximumProgressValue));
	}
	
	@Override
	public int getMaximumProgressValue()
	{
		if (_maximumValue == null)
			throw new IllegalStateException(
				"This task has indeterminate progress.");
		
		return _maximumValue.intValue();
	}

	@Override
	public int getMinimumProgressValue()
	{
		if (_minimumValue == null)
			throw new IllegalStateException(
				"This task has indeterminate progress.");
		
		return _minimumValue.intValue();
	}

	@Override
	public boolean isProgressIndeterminate()
	{
		return _minimumValue == null;
	}
}