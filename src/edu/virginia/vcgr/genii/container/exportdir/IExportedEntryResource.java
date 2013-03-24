package edu.virginia.vcgr.genii.container.exportdir;

import java.sql.Connection;
import java.util.Calendar;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IExportedEntryResource extends IResource
{
	static public QName PATH_CONSTRUCTION_PARAM = new QName(GenesisIIConstants.GENESISII_NS, "path-construction-param");
	static public QName PARENT_IDS_CONSTRUCTION_PARAM = new QName(GenesisIIConstants.GENESISII_NS,
		"parent-ids-construction-param");
	static public QName REPLICATION_INDICATOR = new QName(GenesisIIConstants.GENESISII_NS, "replication_indicator");
	static public QName LAST_MODIFIED_TIME = new QName(GenesisIIConstants.GENESISII_NS, "last-modified-time");
	static public QName REXPORT_RESOLVER_EPR = new QName(GenesisIIConstants.GENESISII_NS, "rexport-resolver-service-epr");

	public String getLocalPath() throws ResourceException;

	public String getId() throws ResourceException;

	public String getParentIds() throws ResourceException;

	public String getReplicationState() throws ResourceException;

	public void destroy(boolean hardDestroy) throws ResourceException, ResourceUnknownFaultType;

	public void destroy(Connection connection, boolean hardDestroy) throws ResourceException, ResourceUnknownFaultType;

	public void setCreateTime(Calendar c) throws ResourceException;

	public void setModTime(Calendar c) throws ResourceException;

	public void setAccessTime(Calendar c) throws ResourceException;
}