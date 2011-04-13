package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.disk.DiskExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.svn.SVNExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.zipjar.ZipJarExportRoot;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

class LightWeightExportUtils
{
	static private String _lastRootDirString = null;
	static private VExportRoot _lastExportRoot = null;
	
	static VExportRoot getRoot()
		throws IOException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		if (rKey != null)
		{
			IResource resource = rKey.dereference();
			String rootDirString = (String)resource.getProperty(
				LightWeightExportConstants.ROOT_DIRECTORY_PROPERTY_NAME);
			if (rootDirString != null)
			{
				synchronized(LightWeightExportUtils.class)
				{
					if (_lastRootDirString != null && 
						_lastRootDirString.equals(rootDirString) &&
						_lastExportRoot != null)
						return _lastExportRoot;
				}
				
				VExportRoot vroot = null;
				
				if (rootDirString.matches("^.+:.*$"))
				{
					// Handle SVN
					Long revision = (Long)resource.getProperty(
							LightWeightExportConstants.SVN_REVISION_PROPERTY_NAME);
							
					vroot = new SVNExportRoot(rootDirString,
						(String)resource.getProperty(
							LightWeightExportConstants.SVN_USER_PROPERTY_NAME),
						(String)resource.getProperty(
							LightWeightExportConstants.SVN_PASS_PROPERTY_NAME),
						revision);
					/* TODO:
					if (revision == null)
						return vroot;
					*/
				} else
				{
					File root = new File(rootDirString);
					if (root.isDirectory())
						return new DiskExportRoot(root);
					else if (root.getName().endsWith(".jar") || 
						root.getName().endsWith(".zip"))
						vroot = new ZipJarExportRoot(root);
				}
				
				synchronized(LightWeightExportUtils.class)
				{
					_lastRootDirString = rootDirString;
					_lastExportRoot = vroot;
					return _lastExportRoot;
				}
			}
		}
		
		throw new IOException(
			"Unable to determine root directory of export.");
	}
	
	static VExportEntry getEntry(String forkPath)
		throws IOException
	{
		VExportRoot root = getRoot();
		return root.lookup(forkPath);
	}
	
	static VExportDir getDirectory(String forkPath) throws IOException
	{
		VExportEntry ret = getEntry(forkPath);
		if (!ret.isDirectory())
			throw new IOException(String.format(
				"Entry \"%s\" is not a directory.", forkPath));
		
		return (VExportDir)ret;
	}
	
	static VExportFile getFile(String forkPath) throws IOException
	{
		VExportEntry ret = getEntry(forkPath);
		if (!ret.isFile())
			throw new IOException(String.format(
				"Entry \"%s\" is not a file.", forkPath));
		
		return (VExportFile)ret;
	}
}