package edu.virginia.vcgr.genii.ui.progress;

public interface Task<ResultType>
{
	public boolean showProgressDialog();

	public void cancel();

	public ResultType execute(TaskProgressListener progressListener) throws Exception;
}
