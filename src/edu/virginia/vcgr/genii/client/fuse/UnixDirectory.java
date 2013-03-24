package edu.virginia.vcgr.genii.client.fuse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fuse.FuseDirEnt;

/*
 * This class is used for caching results of RNS listing in native file-system equivalent format.
 * We use this, instead of directly caching the response of an RNS listing, as directory caching
 * is mostly useful for the FUSE driver where we need a much lighter representation of data than 
 * an RNSEntryResponse.
 * */
public class UnixDirectory
{

	private String path;
	private List<FuseDirEnt> entries;

	public UnixDirectory(String path, Collection<FuseDirEnt> entries)
	{
		this.path = path;
		this.entries = Collections.synchronizedList(new ArrayList<FuseDirEnt>());
		if (entries != null) {
			this.entries.addAll(entries);
		}
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public List<FuseDirEnt> getEntries()
	{
		return entries;
	}

	public void setEntries(List<FuseDirEnt> entries)
	{
		this.entries = entries;
	}

	public void addEntry(FuseDirEnt entry)
	{
		if (entries == null) {
			entries = new ArrayList<FuseDirEnt>();
		}

		// First we have to remove the entry with the same name, if exists; otherwise, there is a
		// chance that
		// the directory will hold multiple entries with the same name. This can happen if the
		// client receive
		// a notification on for its own change by chance.
		String name = entry.name;
		removeEntry(name);

		entries.add(entry);
	}

	public void removeEntry(String name)
	{
		if (entries != null) {
			synchronized (entries) {
				Iterator<FuseDirEnt> iterator = entries.iterator();
				while (iterator.hasNext()) {
					FuseDirEnt entry = iterator.next();
					if (name.equals(entry.name)) {
						iterator.remove();
						break;
					}
				}
			}
		}
	}

	public FuseDirEnt getEntry(String entryName)
	{
		if (entries != null) {
			synchronized (entries) {
				for (FuseDirEnt entry : entries) {
					if (entryName.equals(entry.name)) {
						return entry;
					}
				}
			}
		}
		return null;
	}
}
