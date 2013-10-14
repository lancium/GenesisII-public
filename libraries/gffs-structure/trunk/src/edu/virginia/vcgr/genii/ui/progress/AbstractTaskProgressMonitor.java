package edu.virginia.vcgr.genii.ui.progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

abstract class AbstractTaskProgressMonitor implements ProgressMonitor
{
	private Collection<ProgressMonitorListener> _listeners = new LinkedList<ProgressMonitorListener>();

	final protected void fireTaskStarted()
	{
		Collection<ProgressMonitorListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<ProgressMonitorListener>(_listeners);
		}

		for (ProgressMonitorListener listener : listeners)
			listener.taskStarted();
	}

	final protected void fireTaskCompleted()
	{
		Collection<ProgressMonitorListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<ProgressMonitorListener>(_listeners);
		}

		for (ProgressMonitorListener listener : listeners)
			listener.taskCompleted();
	}

	@Override
	final public void addProgressMonitorListener(ProgressMonitorListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	@Override
	final public void removeProgressMonitorListener(ProgressMonitorListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}
}