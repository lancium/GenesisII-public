package edu.virginia.vcgr.genii.container.q2;

/**
 * This class is used to wait on, and notify threads about scheduling 
 * opportunities.  An opportunity is not a guarantee that a job can
 * be schedule, but rather a strong suspician that one can.  It's up
 * to the scheduler to determine if this is true and to actually do 
 * the matching.
 * 
 * @author mmm2a
 */
public class SchedulingEvent
{
	/**
	 * A simple boolean which keeps track of wether
	 * or not there is an opportunity.
	 */
	private boolean _schedulingEvent = false;
	
	/**
	 * A Java Object instances whose sole purpose in life is to give us
	 * an object to lock, wait on, and signal on.
	 */
	private Object _lock = new Object();
	
	/**
	 * Notify any potential waiters that there is a new scheduling opportunity
	 * available.
	 */
	public void notifySchedulingEvent()
	{
		synchronized(_lock)
		{
			_schedulingEvent = true;
			_lock.notifyAll();
		}
	}
	
	/**
	 * Wait for the next scheduling opportunity, or until a timeout occurs.
	 * 
	 * @param timeout The number of milliseconds to wait before giving up.
	 * 
	 * @return True if there is a scheduling opportunity, false otherwise.
	 * 
	 * @throws InterruptedException
	 */
	public boolean waitSchedulingEvent(long timeout)
		throws InterruptedException
	{
		synchronized(_lock)
		{
			/* If there is already a scheduling opportunity, just
			 * clear it and return true.
			 */
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			/* Otherwise, wait for a notification (or timeout) */
			_lock.wait(timeout);
			
			/* Now, see if we got a scheduling event, or if we just
			 * timed out.
			 */
			if (_schedulingEvent)
			{
				_schedulingEvent = false;
				return true;
			}
			
			return false;
		}
	}
	
	/**
	 * This operation is identical to the one above, except that it never 
	 * times out.
	 * 
	 * @throws InterruptedException
	 */
	public void waitSchedulingEvent()
		throws InterruptedException
	{
		synchronized(_lock)
		{
			while (true)
			{
				if (_schedulingEvent)
				{
					_schedulingEvent = false;
					return;
				}
				
				_lock.wait();
			}
		}
	}
}