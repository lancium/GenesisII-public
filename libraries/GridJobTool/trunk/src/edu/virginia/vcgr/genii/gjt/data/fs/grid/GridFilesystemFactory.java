package edu.virginia.vcgr.genii.gjt.data.fs.grid;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemFactory;

public class GridFilesystemFactory implements FilesystemFactory
{
	@Override
	public Filesystem instantiate(Window owner)
	{
		return new GridFilesystem();
	}
}