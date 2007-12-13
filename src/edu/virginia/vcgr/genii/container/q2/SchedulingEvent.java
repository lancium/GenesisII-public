package edu.virginia.vcgr.genii.container.q2;

public class SchedulingEvent
{
	private boolean _schedulingEvent = false;
	private Object _lock = new Object();
	
	public void notifySchedulingEvent()
	{
		synchronized(_lock)
		{
			_schedulingEvent = true;
			_lock.notifyAll();
		}
	}
	
	public boolean waitSchedulingEvent(long timeout)
		throws InterruptedException
	{
		synchronized(_lock)
		{
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			_lock.wait(timeout);
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			return false;
		}
	}
	
	public boolean waitSchedulingEvent()
		throws InterruptedException
	{
		synchronized(_lock)
		{
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			_lock.wait();
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			return false;
		}
}
}