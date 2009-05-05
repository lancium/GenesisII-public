package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;

public abstract class AbstractVExportRoot implements VExportRoot
{
	protected abstract VExportEntry internalLookup(String normalizedPath)
		throws IOException;
	
	@Override
	final public VExportEntry lookup(String path) throws IOException
	{
		if (path == null)
			path = "";
		
		while (path.startsWith("/"))
			path = path.substring(1);
		
		return internalLookup(path);
	}
}