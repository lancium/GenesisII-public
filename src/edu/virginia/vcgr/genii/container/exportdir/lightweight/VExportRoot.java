package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;

public interface VExportRoot
{
	public VExportEntry lookup(String path) throws IOException;
}