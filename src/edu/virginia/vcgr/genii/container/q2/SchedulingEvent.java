package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

/**
 * This class is used to wait on, and notify threads about scheduling opportunities. An opportunity
 * is not a guarantee that a job can be schedule, but rather a strong suspician that one can. It's
 * up to the scheduler to determine if this is true and to actually do the matching.
 * 
 * @author mmm2a
 */
public class SchedulingEvent
{
	/**
	 * A simple boolean which keeps track of wether or not there is an opportunity.
	 */
	private boolean _schedulingEvent = false;

	/**
	 * A Java Object instances whose sole purpose in life is to give us an object to lock, wait on,
	 * and signal on.
	 */
	private Object _lock = new Object();

	/**
	 * This variable is used when we want to schedule an event to take place sometime in the future.
	 */
	private Date _nextScheduledEvent = null;

	/**
	 * Notify any potential waiters that there is a new scheduling opportunity available.
	 */
	public void notifySchedulingEvent()
	{
		synchronized (_lock) {
			_schedulingEvent = true;
			_lock.notifyAll();
		}
	}

	/**
	 * This operation is identical to the one above, except that it never times out.
	 * 
	 * @throws InterruptedException
	 */
	public void waitSchedulingEvent() throws InterruptedException
	{
		synchronized (_lock) {
			while (true) {
				long timeout = -1L;
				if (_nextScheduledEvent != null)
					timeout = (_nextScheduledEvent.getTime() - System.currentTimeMillis()) + 1000L;

				if (_schedulingEvent) {
					_schedulingEvent = false;
					return;
				}

				if (timeout >= 0L)
					_lock.wait(timeout);
				else
					_lock.wait();

				if (_nextScheduledEvent != null && _nextScheduledEvent.before(new Date())) {
					_nextScheduledEvent = null;
					_schedulingEvent = true;
				}
			}
		}
	}

	public void setScheduledEvent(Date nextScheduledEvent)
	{
		synchronized (_lock) {
			_nextScheduledEvent = nextScheduledEvent;
			_lock.notifyAll();
		}
	}
}