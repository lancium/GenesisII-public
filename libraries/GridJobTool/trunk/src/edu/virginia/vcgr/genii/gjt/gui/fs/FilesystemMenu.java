package edu.virginia.vcgr.genii.gjt.gui.fs;

import javax.swing.JMenu;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

public class FilesystemMenu extends JMenu
{
	static final long serialVersionUID = 0L;

	static final private String MENU_NAME = "Filesystems";

	public FilesystemMenu(FilesystemMap filesystemMap)
	{
		super(MENU_NAME);

		add(new StaticFilesystemAction(filesystemMap, FilesystemType.Default));
		for (FilesystemType type : FilesystemType.values()) {
			if (type == FilesystemType.Default)
				continue;

			if (type.canEdit()) {
				add(new DynamicFilesystemMenu(filesystemMap, type));
			} else {
				add(new StaticFilesystemAction(filesystemMap, type));
			}
		}
	}
}