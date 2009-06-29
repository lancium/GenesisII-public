package org.morgan.util.gui.progress;

public interface ProgressTask<Type>
{
	public boolean isProgressIndeterminate();
	public int getMinimumProgressValue();
	public int getMaximumProgressValue();
	
	public Type compute(ProgressNotifier notifier) throws Exception;
}