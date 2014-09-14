package edu.virginia.vcgr.genii.gjt.data.variables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

public class BasicParameterizable implements Parameterizable
{
	private Collection<ParameterizableListener> _listeners = new LinkedList<ParameterizableListener>();

	public void fireParameterizableStringModified(String oldValue, String newValue)
	{
		Collection<ParameterizableListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<ParameterizableListener>(_listeners);
		}

		for (ParameterizableListener listener : listeners)
			listener.parameterizableStringModified(oldValue, newValue);
	}

	@Override
	public void addParameterizableListener(ParameterizableListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	@Override
	public void removeParameterizableListener(ParameterizableListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}
}