package edu.virginia.vcgr.genii.gjt.data.recent;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

public class Recents
{
	static private final int MAX_RECENTS = 8;
	static private String KEY_PATTERN = "recent-file.%d";

	static public Recents instance = new Recents();

	private Preferences _recentsNode;
	private Vector<File> _recents = new Vector<File>();

	private Recents()
	{
		_recentsNode = Preferences.userNodeForPackage(Recents.class);
		for (int lcv = 0; lcv < MAX_RECENTS; lcv++) {
			String value = _recentsNode.get(String.format(KEY_PATTERN, lcv), null);
			if (value == null)
				break;
			_recents.add(new File(value));
		}
	}

	synchronized public List<File> recents()
	{
		return new Vector<File>(_recents);
	}

	synchronized public void addRecent(File entry)
	{
		entry = entry.getAbsoluteFile();
		LinkedList<File> tmp = new LinkedList<File>(_recents);
		tmp.remove(entry);
		tmp.addFirst(entry);
		while (tmp.size() > MAX_RECENTS)
			tmp.removeLast();

		clear();
		_recents.addAll(tmp);

		int lcv = 0;
		for (File recent : _recents) {
			_recentsNode.put(String.format(KEY_PATTERN, lcv++), recent.getAbsolutePath());
		}
	}

	synchronized public void clear()
	{
		for (int lcv = 0; lcv < MAX_RECENTS; lcv++)
			_recentsNode.remove(String.format(KEY_PATTERN, lcv));
		_recents.clear();
	}
}