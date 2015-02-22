package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a set of restrictions on where exports may be created in the local filesystem.
 */
public class ExportRestrictionsList
{
	File _restrictions = null;

	public ExportRestrictionsList()
	{
		_restrictions = InstallationProperties.getInstallationProperties().getExportCreationRestrictionsFile();
	}

	/**
	 * returns true if the path specified is actually a symbolic link.
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFileSymLink(File path)
	{
		Path nioPath = path.toPath();
		return Files.isSymbolicLink(nioPath);
	}

	// hmmm: implement the rest of this class.

	/**
	 * tests a path to see whether exports are allowed on it or not.
	 */
	boolean checkCreationOkay(String path, String mode)
	{
		///hmmm: implement parsing of the restrictions file.
		
		return true;
	}
}
