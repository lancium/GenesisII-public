package edu.virginia.vcgr.genii.gjt.data.fs.def;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemFactory;

public class DefaultFilesystemFactory implements FilesystemFactory
{
	@Override
	public Filesystem instantiate(Window owner)
	{
		return new DefaultFilesystem();
	}
}