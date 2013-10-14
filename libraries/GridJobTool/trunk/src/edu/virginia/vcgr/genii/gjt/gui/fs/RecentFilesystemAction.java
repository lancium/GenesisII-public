package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.gui.icons.Icons;
import edu.virginia.vcgr.genii.gjt.util.Duple;

class RecentFilesystemAction extends AbstractAction
{
	static final long serialVersionUID = 0L;

	private FilesystemMap _filesystemMap;
	private Filesystem _recent;

	RecentFilesystemAction(FilesystemMap filesystemMap, Duple<Calendar, Filesystem> recent)
	{
		super(recent.second().toString());
		_filesystemMap = filesystemMap;

		_recent = recent.second();

		putValue(
			Action.SMALL_ICON,
			Icons.createTextIcon(new Font(Font.DIALOG, Font.PLAIN, 10), Color.gray, new Dimension(150, 23),
				String.format("%1$tD %1$tr", recent.first())));
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		_filesystemMap.set(_recent);
	}
}