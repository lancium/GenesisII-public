package edu.virginia.vcgr.genii.gjt;

public class BlockingJobToolListener implements JobToolListener
{
	private boolean _closed = false;
	private Object _lock = new Object();

	@Override
	public void jobWindowClosed()
	{
		// We can ignore this one
	}

	@Override
	public void allJobWindowsClosed()
	{
		synchronized (_lock) {
			_closed = true;
			_lock.notifyAll();
		}
	}

	final public void join() throws InterruptedException
	{
		synchronized (_lock) {
			while (!_closed)
				_lock.wait();
		}
	}
}