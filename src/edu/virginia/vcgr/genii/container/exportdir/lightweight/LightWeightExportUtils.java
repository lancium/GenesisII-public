package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.ExportProperties.ExportMechanisms;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.disk.DiskExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoDiskExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoDiskExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoExportUtils;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.svn.SVNExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.zipjar.ZipJarExportRoot;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class LightWeightExportUtils
{
	static private Log _logger = LogFactory.getLog(LightWeightExportUtils.class);

	static private String _lastRootDirString = null;
	static private VExportRoot _lastExportRoot = null;

	static VExportRoot getRoot(ResourceKey myKey) throws IOException
	{
		ResourceKey rKey;
		if (myKey != null)
			rKey = myKey;
		else
			rKey = ResourceManager.getCurrentResource();

		if (rKey != null) {
			IResource resource = rKey.dereference();
			String rootDirString = (String) resource.getProperty(LightWeightExportConstants.ROOT_DIRECTORY_PROPERTY_NAME);
			if (rootDirString != null) {
				synchronized (LightWeightExportUtils.class) {
					if (_lastRootDirString != null && _lastRootDirString.equals(rootDirString) && _lastExportRoot != null)
						return _lastExportRoot;
				}

				VExportRoot vroot = null;

				// future: Temporarily disabled code for svn exports. why was it disabled? -cak
				boolean svnEnabled = false;
				if (svnEnabled && (rootDirString.matches("^.+:.*$"))) {
					// Handle SVN export types.
					Long revision = (Long) resource.getProperty(LightWeightExportConstants.SVN_REVISION_PROPERTY_NAME);
					vroot =
						new SVNExportRoot(rootDirString, (String) resource.getProperty(LightWeightExportConstants.SVN_USER_PROPERTY_NAME),
							(String) resource.getProperty(LightWeightExportConstants.SVN_PASS_PROPERTY_NAME), revision);
					if (revision == null)
						return vroot;
				} else {
					File root = new File(rootDirString);

					ExportMechanisms exportType =
						ExportMechanisms.parse((String) rKey.dereference().getProperty(LightWeightExportConstants.EXPORT_MECHANISM));

					if (exportType == null) {
						/*
						 * if we couldn't retrieve the original mode used for creating the export, we fall back to the original mode since
						 * this is probably an older export.
						 */
						exportType = ExportMechanisms.EXPORT_MECH_ACL;
						if (_logger.isDebugEnabled())
							_logger.debug("did not find stored export mode; falling back to older export creation mode of: "
								+ exportType.toString());
					}
					if (exportType.equals(ExportMechanisms.EXPORT_MECH_PROXYIO)) {
						String unixUsername = SudoExportUtils.getExportOwnerUser(rKey);
						if (unixUsername == null) {
							String msg = "failure to determine the export owner name for a sudo-based export!";
							_logger.error(msg);
							throw new IOException(msg);

						}

						if (SudoDiskExportEntry.isDir(root, unixUsername)) {
							return new SudoDiskExportRoot(root, unixUsername);
						}
					} else if ((exportType.equals(ExportMechanisms.EXPORT_MECH_ACL))
						|| (exportType.equals(ExportMechanisms.EXPORT_MECH_ACLANDCHOWN))) {
						if (root.isDirectory())
							return new DiskExportRoot(root);
						else if (root.getName().endsWith(".jar") || root.getName().endsWith(".zip"))
							vroot = new ZipJarExportRoot(root);
					} else {
						throw new ResourceException("unknown or unimplemented type of export mechanism: " + exportType);
					}
				}

				synchronized (LightWeightExportUtils.class) {
					_lastRootDirString = rootDirString;
					_lastExportRoot = vroot;
					return _lastExportRoot;
				}
			}
		}

		throw new IOException("Unable to determine root directory of export.");
	}

	static VExportEntry getEntry(String forkPath) throws IOException
	{
		VExportRoot root = getRoot(null);
		return root.lookup(forkPath);
	}

	static VExportEntry getEntry(String forkPath, ResourceKey rKey) throws IOException
	{
		if (rKey == null)
			return getEntry(forkPath);

		VExportRoot root = getRoot(rKey);
		return root.lookup(forkPath);
	}

	static VExportDir getDirectory(String forkPath) throws IOException
	{
		VExportEntry ret = getEntry(forkPath);
		if (!ret.isDirectory())
			throw new IOException(String.format("Entry \"%s\" is not a directory.", forkPath));

		return (VExportDir) ret;
	}

	static public VExportDir getDirectory(String forkPath, ResourceKey rKey) throws IOException
	{
		if (rKey == null)
			return getDirectory(forkPath);

		VExportEntry ret = getEntry(forkPath, rKey);

		if (!ret.isDirectory())
			throw new IOException(String.format("Entry \"%s\" is not a directory.", forkPath));

		return (VExportDir) ret;
	}

	static VExportFile getFile(String forkPath) throws IOException
	{
		VExportEntry ret = getEntry(forkPath);
		if (!ret.isFile())
			throw new IOException(String.format("Entry \"%s\" is not a file.", forkPath));

		return (VExportFile) ret;
	}

	static public VExportFile getFile(String forkPath, ResourceKey rKey) throws IOException
	{
		if (rKey == null)
			return getFile(forkPath);

		VExportEntry ret = getEntry(forkPath, rKey);

		if (!ret.isFile())
			throw new IOException(String.format("Entry \"%s\" is not a file.", forkPath));

		return (VExportFile) ret;
	}

}