package edu.virginia.vcgr.genii.container.exportdir.lightweight.disk;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportRoot;

public class DiskExportRoot extends AbstractVExportRoot implements VExportRoot
{
	private File _root;
	
	public DiskExportRoot(File root)
	{
		_root = root;
	}
	
	@Override
	protected VExportEntry internalLookup(String normalizedPath) 
		throws IOException
	{
		if (normalizedPath.length() == 0)
			return new DiskExportEntry(_root);
		
		return new DiskExportEntry(new File(_root, normalizedPath));
	}
}