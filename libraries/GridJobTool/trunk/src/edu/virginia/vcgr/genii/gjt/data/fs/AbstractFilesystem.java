package edu.virginia.vcgr.genii.gjt.data.fs;

import java.awt.Window;

import javax.swing.Icon;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.gui.icons.Icons;
import edu.virginia.vcgr.jsdl.FileSystem;

public abstract class AbstractFilesystem implements Filesystem {
	@XmlTransient
	private FilesystemType _filesystemType;

	@SuppressWarnings("unused")
	private AbstractFilesystem() {
		_filesystemType = null;
	}

	protected boolean editImpl(Window owner) {
		return false;
	}

	protected AbstractFilesystem(FilesystemType filesystemType) {
		_filesystemType = filesystemType;
	}

	@Override
	final public FilesystemType filesystemType() {
		return _filesystemType;
	}

	@Override
	final public boolean edit(Window owner) {
		if (!_filesystemType.canEdit())
			return false;

		return editImpl(owner);
	}

	@Override
	public String toString() {
		return _filesystemType.toString();
	}

	static public Icon getIcon() {
		return Icons.FolderAt24By24;
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException(
				"Clone not supported in this class.");
	}

	@Override
	public FileSystem toJSDLFilesystem() {
		throw new UnsupportedOperationException(String.format(
				"Can't generate JSDL for filesystem %s", this));
	}
}