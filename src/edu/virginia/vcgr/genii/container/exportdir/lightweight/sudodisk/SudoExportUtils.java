package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.container.exportdir.GridMapUserList;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.LightWeightExportConstants;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class SudoExportUtils
{
	static private Log _logger = LogFactory.getLog(SudoExportUtils.class);

	//hmmm: this class needs to use same authorization scheme as the regular export utils.
	
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
		if (users.size() > 0) {
			// currently we always use the first user listed in the grid map.
			return users.get(0);
		} else {
			_logger.debug("did not find grid user in mapfile: " + dnToFind);
			return null;
		}
	}

	/**
	 * reports the unix user that owns the export, for sudo-based exports. this checks the creation
	 * properties for the export to find the info.
	 */
	// hmmm: make new function that provides a cached list of the owners, so we don't call to grab
	// them
	// multiple times. is that class just the one that can load the export init props?
	public static String getExportOwnerUser(ResourceKey key) throws IOException
	{
		String primaryDN = (String) key.dereference().getProperty(LightWeightExportConstants.PRIMARY_OWNER_NAME);		
		String toReturn = doGridMapping(primaryDN);
		if (toReturn == null) {
			String secondaryDN = (String) key.dereference().getProperty(LightWeightExportConstants.SECONDARY_OWNER_NAME);
			toReturn = doGridMapping(secondaryDN);
		}

		return toReturn;
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
}
