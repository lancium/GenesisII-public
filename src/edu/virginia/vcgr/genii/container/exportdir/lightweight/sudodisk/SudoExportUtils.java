package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.ExportProperties;
import edu.virginia.vcgr.genii.client.ExportProperties.ExportMechanisms;
import edu.virginia.vcgr.genii.client.ExportProperties.OwnershipForByteIO;
import edu.virginia.vcgr.genii.client.security.WalletUtilities;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.container.exportdir.GridMapUserList;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.LightWeightExportConstants;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class SudoExportUtils
{
	static private Log _logger = LogFactory.getLog(SudoExportUtils.class);

	/**
	 * Returns the local Unix username given the username from the calling context.
	 */
	public static String doGridMapping(String dnToFind)
	{
		if (dnToFind == null) {
			_logger.error("attempted mapping of 'null' as the DN owning the export.  this is broken.");
			return null;
		}

		GridMapUserList users = GffsExportConfiguration.mapDistinguishedName(dnToFind);
		if ((users != null) && (users.size() > 0)) {
			// currently we always use the first user listed in the grid map.
			return users.get(0);
		} else {
			_logger.debug("did not find grid user in mapfile: " + dnToFind);
			return null;
		}
	}

	/**
	 * similar to doGridMapping above, but operates on a list of possible users. we support this in
	 * order to map both a regular grid user and an appropriate TLS identity, depending on which one
	 * is found in grid map file.
	 */
	public static String doGridMapping(List<String> dnsToFind)
	{
		if (dnsToFind == null) {
			_logger.error("attempted mapping of 'null' list as the DN owning the export.  this is broken.");
			return null;
		}
		for (String user : dnsToFind) {
			GridMapUserList users = GffsExportConfiguration.mapDistinguishedName(user);
			if ((users != null) && (users.size() > 0)) {
				// currently we always use the first user listed in the grid map.
				return users.get(0);
			}
		}
		// no one relevant was found.
		return null;
	}

	/**
	 * reports the unix user that owns the export, for sudo-based exports. this checks the creation
	 * properties for the export to find the info.
	 */
	public static String getExportOwnerUser(ResourceKey key) throws IOException
	{
		return (String) key.dereference().getProperty(LightWeightExportConstants.EXPORT_OWNER_UNIX_NAME);
	}

	public static boolean dirReadable(String path, ResourceKey key) throws IOException
	{
		if (path == null) {
			return false;
		}

		File target = new File(path);

		String uname = getExportOwnerUser(key);
		if (_logger.isDebugEnabled())
			_logger.debug("using unix user '" + uname + "' for sudo-based export at: " + path);
		if (uname == null) {
			String msg = "failed to find owner of sudo-based export on path: " + path;
			_logger.error(msg);
			return false;
		}

		if (SudoDiskExportEntry.doesExist(target, uname) && SudoDiskExportEntry.isDir(target, uname)
			&& SudoDiskExportEntry.canRead(path, uname)) {
			return true;
		}

		return false;
	}

	/**
	 * changes ownership of a file (i.e., chowns it) to a specific unix user.
	 */
	public static void chownFileToUser(File localPath, String unixUser) throws IOException
	{
		if (localPath == null) {
			_logger.error("path to chown is null.");
			return;
		}
		if (unixUser == null) {
			_logger.error("the unix user to chown to is null.");
			return;
		}
		if (!localPath.exists()) {
			_logger.error("file does not exist yet, cannot be chowned: " + localPath);
			return;
		}

		// look up our script that does the chown work.
		File gffschowner = new File(ExportProperties.getExportProperties().getGffsChownFilePath());
		if (gffschowner.exists() && gffschowner.canExecute()) {
			_logger.debug("changing owner to '" + unixUser + "' on path: '" + localPath + "'");

			// make an array of our command line parameters, which saves playing with quotes.
			String cmdlines[] = { "sudo", gffschowner.getAbsolutePath(), unixUser, localPath.getAbsolutePath() };
			// run the chown operation and then wait for it to finish.
			Process p = Runtime.getRuntime().exec(cmdlines);
			try {
				int waitResult = p.waitFor();
				if (waitResult != 0) {
					_logger.error("received a failure code of " + waitResult + " when running gffschown to user '" + unixUser
						+ "'");
				}
			} catch (InterruptedException e) {
				_logger.warn("exception occurred while awaiting gffs chown call", e);
			}
		} else {
			_logger.warn("cannot find gffschown application in configured properties.");
		}
	}

	/**
	 * changes ownership of a file to the owning unix user, if we can intuit / deduce that owner
	 * using the existing credentials and the grid-mapfile.
	 */
	public static void chownFileToDeducedUser(File localPath) throws IOException
	{
		ArrayList<String> users = WalletUtilities.extractOwnersFromCredentials(null, true);
		String unixUser = SudoExportUtils.doGridMapping(users);
		if (unixUser != null) {
			chownFileToUser(localPath, unixUser);
		} else {
			_logger.warn("chown attempt could not deduce owner from credentials for: " + localPath);
		}
	}

	/**
	 * checks to see if regular or exported byteio files should be chowned to their unix user on
	 * creation. if so, this function does the chown.
	 * 
	 * @throws IOException
	 */
	public static void chownIfChownToUserEnabled(File localPath) throws IOException
	{
		if (ExportProperties.getExportProperties().getByteIOStorage() == OwnershipForByteIO.BYTEIO_CHOWNTOUSER) {
			_logger.debug("chown for byteio storage on path: '" + localPath + "'");
			chownFileToDeducedUser(localPath);
		}
	}

	/**
	 * checks to see if regular or exported byteio files should be chowned to their unix user on
	 * creation. if so, this function does the chown.
	 * 
	 * @throws IOException
	 */
	public static void chownIfACLandChownEnabled(File localPath) throws IOException
	{
		if (ExportProperties.getExportProperties().getExportMechanism() == ExportMechanisms.EXPORT_MECH_ACLANDCHOWN) {
			_logger.debug("chown for export on path: '" + localPath + "'");
			chownFileToDeducedUser(localPath);
		}
	}
}
