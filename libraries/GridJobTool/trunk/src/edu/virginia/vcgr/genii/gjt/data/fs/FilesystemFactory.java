package edu.virginia.vcgr.genii.gjt.data.fs;

import java.awt.Window;

public interface FilesystemFactory {
	public Filesystem instantiate(Window owner);
}