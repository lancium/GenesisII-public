package edu.virginia.vcgr.genii.ui.progress;

public interface TaskCompletionListener<ResultType>
{
	public void taskCompleted(Task<ResultType> task, ResultType result);

	public void taskCancelled(Task<ResultType> task);

	public void taskExcepted(Task<ResultType> task, Throwable cause);
}