package edu.virginia.vcgr.genii.container.replicatedExport;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.ggf.rns.RNSEntryExistsFaultType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public interface IRExportResource extends IRExportEntryResource
{
	static public QName LOCALPATH_CONSTRUCTION_PARAM = 
		new QName(GenesisIIConstants.GENESISII_NS, "localpath-construction-param");
	static public QName PARENT_IDS_CONSTRUCTION_PARAM = 
		new QName(GenesisIIConstants.GENESISII_NS, "parent-ids-construction-param");
	
	public String getLocalPath() throws ResourceException;
	public String getId() throws ResourceException;
	
	public Collection<RExportEntry> retrieveEntries(String regex)
		throws ResourceException;
	
	public void addEntry(RExportEntry entry, boolean createOnDisk)
		throws ResourceException, RNSEntryExistsFaultType;
	
	public void setLocalPath(String localPath) throws ResourceException;
}
