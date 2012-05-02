package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.morgan.util.io.GuaranteedDirectory;

public class ByteIOFileCreator
{
	static private Random _directoryBalancer = new Random();
	static private final int DISPERSION_LEVELS = 2;
	static private final int DISPERSION_WIDTH = 32;
	
	/**
	 * Create a new file in the user directory for saving byteIO data.
	 */
	synchronized public static File createFile(File userDir)
		throws IOException
	{
		File baseDir = new GuaranteedDirectory(userDir, "rbyteio-data");
		String filePrefix = "rbyteio";
		String fileSuffix = ".dat";
		for (int lcv = 0; lcv < DISPERSION_LEVELS; lcv++)
		{
			int value = _directoryBalancer.nextInt(DISPERSION_WIDTH);
			baseDir = new GuaranteedDirectory(baseDir, String.format("dir.%d", value));
		}
		return File.createTempFile(filePrefix, fileSuffix, baseDir);
	}
	
	public static String getRelativePath(File userDir, File newFile)
	{
		String userPath = userDir.getAbsolutePath() + File.separator;
		String path = newFile.getAbsolutePath();
		if (path.startsWith(userPath))
		{
			return path.substring(userPath.length());
		}
		return path;
	}
	
	public static File getAbsoluteFile(File userDir, String path)
	{
		File file = new File(path);
		return(file.isAbsolute() ? file : new File(userDir, path));
	}
}
