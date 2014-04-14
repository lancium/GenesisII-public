package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportRoot;

public class SudoDiskExportRoot extends AbstractVExportRoot implements VExportRoot
{
	private File _root;
	private String _uname;

	public SudoDiskExportRoot(File root, String uname)
	{
		_root = root;
		_uname = uname;
	}

	@Override
	protected VExportEntry internalLookup(String normalizedPath) throws IOException
	{
		if (normalizedPath.length() == 0)
			return new SudoDiskExportEntry(_root, _uname);

		return new SudoDiskExportEntry(new File(_root, normalizedPath),
				_uname);
	}
}