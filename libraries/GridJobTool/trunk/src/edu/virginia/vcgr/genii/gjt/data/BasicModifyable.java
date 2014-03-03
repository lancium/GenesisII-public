package edu.virginia.vcgr.genii.gjt.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

public class BasicModifyable implements Modifyable {
	private Collection<ModificationListener> _listeners = new LinkedList<ModificationListener>();

	public void fireJobDescriptionModified() {
		Collection<ModificationListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<ModificationListener>(_listeners);
		}

		for (ModificationListener listener : listeners)
			listener.jobDescriptionModified();
	}

	@Override
	public void addModificationListener(ModificationListener listener) {
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	@Override
	public void removeModificationListener(ModificationListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}
}