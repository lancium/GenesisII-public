package edu.virginia.vcgr.genii.client.io;

import java.io.File;

public class FileUtils
{
	static public void recursivelyRemove(File target)
	{
		if (target.exists())
		{
			if (target.isDirectory())
			{
				for (File file : target.listFiles())
				{
					recursivelyRemove(file);
				}
			}
			
			target.delete();
		}
	}
}