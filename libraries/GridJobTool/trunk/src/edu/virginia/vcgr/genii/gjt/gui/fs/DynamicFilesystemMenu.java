package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemListener;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemRecents;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.gui.icons.Icons;
import edu.virginia.vcgr.genii.gjt.util.Duple;

class DynamicFilesystemMenu extends JMenu
{
	static final long serialVersionUID = 0L;

	private FilesystemMap _filesystemMap;
	private FilesystemType _filesystemType;

	private void configureState()
	{
		String name;
		Icon icon;

		Filesystem filesystem = _filesystemMap.get(_filesystemType);

		if (filesystem != null) {
			name = filesystem.toString();
			icon = Icons.CheckMarkAt16By16;
		} else {
			name = _filesystemType.toString();
			icon = Icons.EmptyAt16By16;
		}

		setText(name);
		setIcon(icon);
	}

	public DynamicFilesystemMenu(FilesystemMap filesystemMap, FilesystemType filesystemType)
	{
		_filesystemMap = filesystemMap;
		_filesystemType = filesystemType;

		addMenuListener(new MenuListenerImpl());
		filesystemMap.addFilesystemListener(new FilesystemListenerImpl());

		configureState();
	}

	private class FilesystemListenerImpl implements FilesystemListener
	{
		@Override
		public void filesystemDefined(FilesystemMap filesystemMap, Filesystem newFilesystem)
		{
			if (newFilesystem.filesystemType() == _filesystemType)
				configureState();
		}
	}

	private class MenuListenerImpl implements MenuListener
	{
		@Override
		public void menuCanceled(MenuEvent e)
		{
			// Do nothing
		}

		@Override
		public void menuDeselected(MenuEvent e)
		{
			// Do nothing
		}

		@Override
		public void menuSelected(MenuEvent e)
		{
			removeAll();

			add(new NewFilesystemDefinitionAction(_filesystemMap, _filesystemType));
			add(new EditCurrentAction(_filesystemMap.get(_filesystemType)));
			addSeparator();
			for (Duple<Calendar, Filesystem> recent : FilesystemRecents.instance(_filesystemType).recents())
				add(new RecentFilesystemAction(_filesystemMap, recent));
		}
	}

	private class EditCurrentAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private EditCurrentAction(Filesystem filesystem)
		{
			super(String.format("Edit %s", (filesystem == null) ? _filesystemType : filesystem));

			setEnabled(filesystem != null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Filesystem filesystem = _filesystemMap.get(_filesystemType);
			if (filesystem.edit(SwingUtilities.getWindowAncestor((Component) e.getSource())))
				_filesystemMap.set(filesystem);
		}
	}
}