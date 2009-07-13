package edu.virginia.vcgr.genii.container.iterator;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class IteratorUtils
{
	static public File getContentDirectory()
	{
		File userDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
		userDir = new File(userDir, "iterators");
		if (!userDir.exists())
			userDir.mkdirs();
		if (!userDir.exists())
			throw new RuntimeException(
				"Unable to create iterator content directory.");
		
		return userDir;
	}
	
	static public File createContentFile() throws IOException
	{
		return File.createTempFile("iter", ".dat", getContentDirectory());
	}
	
	static public File getContentFile(String filename)
	{
		return new File(getContentDirectory(), filename);
	}
}