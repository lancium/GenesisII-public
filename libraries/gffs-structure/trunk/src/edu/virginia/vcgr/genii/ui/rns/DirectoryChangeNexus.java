package edu.virginia.vcgr.genii.ui.rns;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.SwingUtilities;

public class DirectoryChangeNexus
{
	private Collection<DirectoryChangeListener> _listeners = new LinkedList<DirectoryChangeListener>();

	public void addDirectoryChangeListener(DirectoryChangeListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	public void removeDirectoryChangeListener(DirectoryChangeListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	public void fireDirectoryChanged(String parentPath)
	{
		Collection<DirectoryChangeListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<DirectoryChangeListener>(_listeners);
		}

		new NotificationRunner(parentPath, listeners).run();
	}

	static private class NotificationRunner implements Runnable
	{
		private String _parentDirectory;
		private Collection<DirectoryChangeListener> _listeners;

		private NotificationRunner(String parentDirectory, Collection<DirectoryChangeListener> listeners)
		{
			_parentDirectory = parentDirectory;
			_listeners = listeners;
		}

		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(this);
				return;
			}

			for (DirectoryChangeListener listener : _listeners)
				listener.contentsChanged(_parentDirectory);
		}
	}
}