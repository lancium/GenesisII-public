package edu.virginia.vcgr.genii.gjt.data.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.genii.gjt.util.Duple;
import edu.virginia.vcgr.genii.gjt.util.IOUtils;

public class FilesystemRecents {
	static private Logger _logger = Logger.getLogger(FilesystemRecents.class);

	static private final String KEY_FORMAT = "recent-filesystem.%d";
	static private final int MAX_RECENTS = 8;

	static private Map<FilesystemType, FilesystemRecents> _recents = new EnumMap<FilesystemType, FilesystemRecents>(
			FilesystemType.class);

	static private byte[] toBytes(Duple<Calendar, Filesystem> entry)
			throws IOException {
		ObjectOutputStream oos = null;

		if (entry == null)
			return null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeLong(entry.first().getTimeInMillis());
			oos.writeObject(entry.second());
			oos.close();
			return baos.toByteArray();
		} finally {
			IOUtils.close(oos);
		}
	}

	static private Duple<Calendar, Filesystem> fromBytes(byte[] bytes)
			throws IOException, ClassNotFoundException {
		if (bytes == null)
			return null;

		ObjectInputStream ois = null;

		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(ois.readLong());
			return new Duple<Calendar, Filesystem>(cal,
					(Filesystem) ois.readObject());
		} finally {
			IOUtils.close(ois);
		}
	}

	static public FilesystemRecents instance(FilesystemType filesystemType) {
		FilesystemRecents recents;

		synchronized (_recents) {
			recents = _recents.get(filesystemType);
			if (recents == null)
				_recents.put(filesystemType, recents = new FilesystemRecents(
						filesystemType));
		}

		return recents;
	}

	private Preferences _preferenceNode;
	private Vector<Duple<Calendar, Filesystem>> _recentEntries = new Vector<Duple<Calendar, Filesystem>>();

	private FilesystemRecents(FilesystemType filesystemType) {
		Preferences root = Preferences
				.userNodeForPackage(FilesystemRecents.class);
		_preferenceNode = root.node(filesystemType.name());

		for (int lcv = 0; lcv < MAX_RECENTS; lcv++) {
			try {
				Duple<Calendar, Filesystem> entry = fromBytes(_preferenceNode
						.getByteArray(String.format(KEY_FORMAT, lcv), null));
				if (entry == null)
					break;

				_recentEntries.add(entry);
			} catch (Throwable cause) {
				_logger.warn(
						"Error attempting to read recent filesystem entry.",
						cause);
			}
		}
	}

	synchronized public List<Duple<Calendar, Filesystem>> recents() {
		return new Vector<Duple<Calendar, Filesystem>>(_recentEntries);
	}

	synchronized public void add(Filesystem entry) {
		entry = (Filesystem) entry.clone();
		LinkedList<Duple<Calendar, Filesystem>> tmp = new LinkedList<Duple<Calendar, Filesystem>>(
				_recentEntries);
		Iterator<Duple<Calendar, Filesystem>> iter = tmp.iterator();
		while (iter.hasNext()) {
			if (iter.next().second().equals(entry)) {
				iter.remove();
				break;
			}
		}

		Calendar timestamp = Calendar.getInstance();
		tmp.addFirst(new Duple<Calendar, Filesystem>(timestamp, entry));
		while (tmp.size() > MAX_RECENTS)
			tmp.removeLast();

		clear();
		_recentEntries.addAll(tmp);

		int lcv = 0;
		for (Duple<Calendar, Filesystem> recent : _recentEntries) {
			try {
				_preferenceNode.putByteArray(String.format(KEY_FORMAT, lcv++),
						toBytes(recent));
			} catch (Throwable cause) {
				_logger.warn("Unable to store recent filesystem entry.", cause);
			}
		}
	}

	synchronized public void clear() {
		for (int lcv = 0; lcv < MAX_RECENTS; lcv++)
			_preferenceNode.remove(String.format(KEY_FORMAT, lcv));
		_recentEntries.clear();
	}
}