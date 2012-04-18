package edu.virginia.vcgr.genii.container.sync;

import javax.xml.namespace.QName;

public class SyncProperty
{
	static public final String RESOURCE_SYNC_NS = "http://vcgr.cs.virginia.edu/genii/resource-sync";
	
	static public final String PRIMARY_EPR_PROP_NAME =
		"edu.virginia.vcgr.genii.sync.primary-epr";
	static public final String TARGET_ID_PROP_NAME =
		"edu.virginia.vcgr.genii.sync.target-id";
	static public final String VERSION_VECTOR_PROP_NAME =
		"edu.virginia.vcgr.genii.sync.version-vector";
	static public final String ERROR_STATE_PROP_NAME =
		"edu.virginia.vcgr.genii.sync.error-state";
	static public final String IS_DESTROYED_PROP_NAME =
		"edu.virginia.vcgr.genii.sync.is-destroyed";
	
	static public final QName VERSION_VECTOR_QNAME =
		new QName(RESOURCE_SYNC_NS, "VersionVector");
	static public final QName REPLICATION_STATUS_QNAME =
		new QName(RESOURCE_SYNC_NS, "ReplicationStatus");
}
