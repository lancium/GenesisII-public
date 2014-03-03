package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;
import java.util.Calendar;

import javax.swing.JPopupMenu;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemRecents;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.util.Duple;

public class PopupFilesystemMenu extends JPopupMenu {
	static final long serialVersionUID = 0L;

	public PopupFilesystemMenu(FilesystemMap filesystemMap,
			FilesystemType filesystemType) {
		super(filesystemType.toString());

		add(new NewFilesystemDefinitionAction(filesystemMap, filesystemType));
		addSeparator();
		for (Duple<Calendar, Filesystem> recent : FilesystemRecents.instance(
				filesystemType).recents())
			add(new RecentFilesystemAction(filesystemMap, recent));
	}

	@Override
	public void show(Component component, int x, int y) {
		super.show(component, x, y);
	}
}