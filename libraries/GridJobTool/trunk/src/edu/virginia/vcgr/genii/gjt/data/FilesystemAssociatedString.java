package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public class FilesystemAssociatedString extends ParameterizableString implements
		FilesystemAssociatedItem {
	@XmlAttribute(name = "filesystem-type", required = false)
	private FilesystemType _filesystemType = null;

	public FilesystemAssociatedString() {
	}

	public FilesystemAssociatedString(ParameterizableBroker pBroker,
			ModificationBroker mBroker) {
		super(pBroker, mBroker);
	}

	@Override
	public void setFilesystemType(FilesystemType filesystemType) {
		if (filesystemType != _filesystemType) {
			_filesystemType = filesystemType;
			fireJobDescriptionModified();
		}
	}

	@Override
	@XmlTransient
	public FilesystemType getFilesystemType() {
		return _filesystemType;
	}
}
