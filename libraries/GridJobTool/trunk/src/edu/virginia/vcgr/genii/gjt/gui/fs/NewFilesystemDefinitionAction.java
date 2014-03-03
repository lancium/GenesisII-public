package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

class NewFilesystemDefinitionAction extends AbstractAction {
	static final long serialVersionUID = 0L;

	private FilesystemMap _filesystemMap;
	private FilesystemType _filesystemType;

	NewFilesystemDefinitionAction(FilesystemMap filesystemMap,
			FilesystemType filesystemType) {
		super("New Definition");

		_filesystemMap = filesystemMap;
		_filesystemType = filesystemType;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Filesystem fs = _filesystemMap.get(_filesystemType);
		if (fs == null)
			_filesystemMap
					.get(SwingUtilities.getWindowAncestor((Component) e
							.getSource()), _filesystemType);
		else if (fs.edit(SwingUtilities.getWindowAncestor((Component) e
				.getSource()))) {
			_filesystemMap.set(fs);
		}
	}
}