package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;

class HistoryEventFilter
{
	private Collection<HistoryEventFilterListener> _listeners = new LinkedList<HistoryEventFilterListener>();

	private HistoryEventLevel _levelFilter;
	private Set<HistoryEventCategory> _categoryFilter;

	protected void fireFilterChanged()
	{
		Collection<HistoryEventFilterListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<HistoryEventFilterListener>(_listeners);
		}

		for (HistoryEventFilterListener listener : listeners)
			listener.filterChanged(this);
	}

	HistoryEventFilter(HistoryEventLevel defaultLevelFilter, Set<HistoryEventCategory> defaultCategoryFilter)
	{
		_levelFilter = defaultLevelFilter;
		_categoryFilter = defaultCategoryFilter;
	}

	final void addFilterListener(HistoryEventFilterListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	final void removeFilterListener(HistoryEventFilterListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	final void levelFilter(HistoryEventLevel newLevelFilter)
	{
		_levelFilter = newLevelFilter;
		fireFilterChanged();
	}

	final HistoryEventLevel levelFilter()
	{
		return _levelFilter;
	}

	final void categoryFilter(Set<HistoryEventCategory> newCategoryFilter)
	{
		_categoryFilter = newCategoryFilter;
		fireFilterChanged();
	}

	final Set<HistoryEventCategory> categoryFilter()
	{
		return _categoryFilter;
	}
}