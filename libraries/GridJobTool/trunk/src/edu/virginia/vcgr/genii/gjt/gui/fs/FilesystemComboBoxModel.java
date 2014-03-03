package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemListener;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

@SuppressWarnings("rawtypes")
class FilesystemComboBoxModel extends DefaultComboBoxModel {
	static final long serialVersionUID = 0L;

	static private Vector<FilesystemType> getItems(FilesystemMap map) {
		Vector<FilesystemType> types = new Vector<FilesystemType>(
				FilesystemType.values().length);
		types.add(FilesystemType.Default);

		for (FilesystemType type : FilesystemType.values()) {
			if (type != FilesystemType.Default && map.get(type) != null)
				types.add(type);
		}

		return types;
	}

	private Set<FilesystemType> _knownSet = EnumSet
			.noneOf(FilesystemType.class);

	@SuppressWarnings("unchecked")
	FilesystemComboBoxModel(FilesystemMap map) {
		super(getItems(map));

		for (int lcv = 0; lcv < getSize(); lcv++) {
			_knownSet.add((FilesystemType) getElementAt(lcv));
		}

		map.addFilesystemListener(new FilesystemListenerImpl());
	}

	private class FilesystemListenerImpl implements FilesystemListener {
		@SuppressWarnings("unchecked")
		@Override
		public void filesystemDefined(FilesystemMap filesystemMap,
				Filesystem newFilesystem) {
			if (_knownSet.contains(newFilesystem.filesystemType()))
				return;

			int index = 1;
			for (FilesystemType type : FilesystemType.values()) {
				if (type == newFilesystem.filesystemType()) {
					if (index >= getSize())
						addElement(type);
					else
						insertElementAt(type, index);

					_knownSet.add(newFilesystem.filesystemType());
					return;
				} else if (type != FilesystemType.Default)
					index++;
			}
		}
	}
}