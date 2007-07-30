package edu.virginia.vcgr.genii.client.io;

import java.io.File;

public class PreferredFiles
{
	static public File getPreferredFile(File preferredBase, File globalBase,
		String subPath)
	{
		File ret = new File(preferredBase, subPath);
		if (!ret.exists() || !ret.isFile())
			ret = new File(globalBase, subPath);
		
		return ret;
	}
}