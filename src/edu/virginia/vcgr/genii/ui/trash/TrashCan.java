package edu.virginia.vcgr.genii.ui.trash;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.persist.Persistence;
import edu.virginia.vcgr.genii.ui.persist.PersistenceDirectory;
import edu.virginia.vcgr.genii.ui.persist.PersistenceKey;

public class TrashCan
{
	static final private String PERSISTENCE_DIRECTORY_NAME = "trash";

	private Collection<TrashCanStateListener> _listeners = new LinkedList<TrashCanStateListener>();

	private PersistenceDirectory _pDirectory;
	private Map<PersistenceKey, Pair<String, PersistenceKey>> _entries = new HashMap<PersistenceKey, Pair<String, PersistenceKey>>();

	protected void fireTrashCanEmptied()
	{
		Collection<TrashCanStateListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<TrashCanStateListener>(_listeners);
		}

		for (TrashCanStateListener listener : listeners)
			listener.trashCanEmptied();
	}

	protected void fireTrashCanFilled()
	{
		Collection<TrashCanStateListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<TrashCanStateListener>(_listeners);
		}

		for (TrashCanStateListener listener : listeners)
			listener.trashCanFilled();
	}

	public TrashCan() throws IOException
	{
		_pDirectory = Persistence.persistence().directory(PERSISTENCE_DIRECTORY_NAME);

		for (PersistenceKey key : _pDirectory.keys()) {
			_entries.put(key, new Pair<String, PersistenceKey>(TrashCanEntry.readPath(key), key));
		}
	}

	public void addTrashCanStateListener(TrashCanStateListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	public void removeTrashCanStateListener(TrashCanStateListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	public boolean isEmpty()
	{
		synchronized (_entries) {
			return _entries.isEmpty();
		}
	}

	public void add(UIContext sourceContext, RNSPath entry) throws IOException
	{
		int size;
		PersistenceKey key = _pDirectory.addEntry(new TrashCanEntry(sourceContext.callingContext(), entry));

		synchronized (_entries) {
			_entries.put(key, new Pair<String, PersistenceKey>(entry.pwd(), key));
			size = _entries.size();
		}

		if (size == 1)
			fireTrashCanFilled();
	}

	public void remove(PersistenceKey key)
	{
		int afterSize;

		synchronized (_entries) {
			_entries.remove(key);
			afterSize = _entries.size();
		}

		_pDirectory.removeEntry(key);
		if (afterSize == 0)
			fireTrashCanEmptied();
	}

	public Collection<Pair<String, PersistenceKey>> contents()
	{
		synchronized (_entries) {
			return new Vector<Pair<String, PersistenceKey>>(_entries.values());
		}
	}
}