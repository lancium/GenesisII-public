package edu.virginia.vcgr.genii.gjt.data;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

public interface FilesystemAssociatedItem {
	public FilesystemType getFilesystemType();

	public void setFilesystemType(FilesystemType filesystemType);
}