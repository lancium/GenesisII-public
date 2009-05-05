package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.util.Collection;

public interface VExportDir extends VExportEntry
{
	public boolean createFile(String newFileName)
		throws IOException;
	public boolean mkdir(String newDirName)
		throws IOException;
	public boolean remove(String entryName)
		throws IOException;
	public Collection<VExportEntry> list(String name)
		throws IOException;
}