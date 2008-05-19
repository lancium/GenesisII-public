package edu.virginia.vcgr.genii.container.exportdir;

import java.util.Collection;

import org.ggf.rns.RNSEntryExistsFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public interface IExportedDirResource extends IExportedEntryResource
{
	public void addEntry(ExportedDirEntry entry, boolean createOnDisk)
		throws ResourceException, RNSEntryExistsFaultType;
	public Collection<String> listEntries() throws ResourceException;
	public Collection<ExportedDirEntry> retrieveEntries(String entryName)
		throws ResourceException;
	public Collection<String> removeEntries(String entryName, boolean hardDestroy)
		throws ResourceException;
	public void getAndSetModifyTime()
		throws ResourceException; 
}