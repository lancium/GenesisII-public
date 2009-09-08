package edu.virginia.vcgr.genii.ui.progress;

public abstract class AbstractTask<ResultType> implements Task<ResultType>
{
	private boolean _cancelled = false;
	
	protected boolean wasCancelled()
	{
		return _cancelled;
	}
	
	@Override
	public void cancel()
	{
		_cancelled = true;
	}

	@Override
	public boolean showProgressDialog()
	{
		return true;
	}
}