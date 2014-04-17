package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils;

import java.io.File;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.file.FileHandler;

public class PSFileWriter
{

	public static String log = "/tmp/proxy.log";

	/**
	 * Writes a string followed by \n to a log file
	 * 
	 * @param str
	 */
	public static void writeToFile(String str)
	{
		if (str == null) {
			return;
		}

		str = str + "\n";
		FileHandler.write(log, str.getBytes(), new File(log).length(), null);

	}

}
