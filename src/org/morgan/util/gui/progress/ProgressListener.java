package org.morgan.util.gui.progress;

public interface ProgressListener<Type>
{
	public void taskCompleted(Type result);
	public void taskCancelled();
	public void taskExcepted(Exception e);
}