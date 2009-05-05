package edu.virginia.vcgr.genii.container.exportdir.lightweight.zipjar;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportRoot;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;

public class ZipJarExportRoot extends AbstractVExportRoot
{
	private File _zipFileTarget;
	private ZipFile _zipFile;
	private Map<String, Map<String, ZipEntry>> _directoryMap;
	
	static private String formPath(String []elements, int length)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			if (builder.length() != 0)
				builder.append('/');
			builder.append(elements[lcv]);
		}
		
		return builder.toString();
	}
	
	static private void addToDirectoryMap(
		Map<String, Map<String, ZipEntry>> map,
		String []path, int length, ZipEntry entry)
	{
		if (length == 0)
			return;
		
		addToDirectoryMap(map, path, length - 1, null);
		String pwd = formPath(path, length - 1);
		Map<String, ZipEntry> directory = map.get(pwd);
		if (directory == null)
			map.put(pwd, directory = new HashMap<String, ZipEntry>());
		String name = path[length - 1];
		if (!directory.containsKey(name))
		{
			if (entry != null && entry.isDirectory())
				entry = null;
			directory.put(name, entry);
		}
	}
	
	public ZipJarExportRoot(File zipFile) 
		throws ZipException, IOException
	{
		_zipFileTarget = zipFile;
		_zipFile = new ZipFile(zipFile);
		_directoryMap = new HashMap<String, Map<String,ZipEntry>>();
		
		Enumeration<? extends ZipEntry> entries = _zipFile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			String path = entry.getName();
			if (path.endsWith("/"))
				path = path.substring(0, path.length() - 1);
			String []elements = path.split("/");
			
			addToDirectoryMap(_directoryMap,
				elements, elements.length, entry);
		}
	}
	
	@Override
	protected VExportEntry internalLookup(String normalizedPath)
			throws IOException
	{
		return new ZipJarEntry(
			_zipFileTarget, _zipFile, _directoryMap, normalizedPath);
	}
}