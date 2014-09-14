package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemListener;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.gui.icons.Icons;

class StaticFilesystemAction extends AbstractAction
{
	static final long serialVersionUID = 0L;

	private FilesystemMap _filesystemMap;
	private FilesystemType _filesystemType;

	private void configureButtonState()
	{
		String name;
		Icon icon;

		Filesystem definition = _filesystemMap.get(_filesystemType);
		if (definition != null) {
			name = definition.toString();
			icon = Icons.CheckMarkAt16By16;
		} else {
			name = _filesystemType.toString();
			icon = Icons.EmptyAt16By16;
		}

		putValue(Action.NAME, name);
		putValue(Action.SMALL_ICON, icon);
		setEnabled(_filesystemType != FilesystemType.Default);
	}

	StaticFilesystemAction(FilesystemMap filesystemMap, FilesystemType filesystemType)
	{
		_filesystemMap = filesystemMap;
		_filesystemType = filesystemType;

		filesystemMap.addFilesystemListener(new FilesystemListenerImpl());
		configureButtonState();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		_filesystemMap.get(SwingUtilities.getWindowAncestor((Component) e.getSource()), _filesystemType);
	}

	private class FilesystemListenerImpl implements FilesystemListener
	{
		@Override
		public void filesystemDefined(FilesystemMap filesystemMap, Filesystem newFilesystem)
		{
			if (newFilesystem.filesystemType() == _filesystemType)
				configureButtonState();
		}
	}
}