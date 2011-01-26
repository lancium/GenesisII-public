package edu.virginia.vcgr.genii.cloud;

import javax.xml.namespace.QName;

public interface CloudConstants{
	
	static public final String GENII_CLOUDBES_NS =
		"http://vcgr.cs.virginia.edu/genii/2010/10/bes/cloud";

	
	static public final String STATUS_NAME =
		"Status";
	static public final String VM_STATUS_NAME =
		"VMStatus";
	static public final String RESOURCE_KILL_NAME =
		"ResourceKill";
	static public final String VM_STATUS_COLLECTION_NAME =
		"VMStatusCollection";
	static public final String SPAWN_RESOURCES_NAME =
		"SpawnResources";
	static public final String SHRINK_RESOURCES_NAME =
		"ShrinkResources";
	static public final String VM_INFO_NAME =
		"VMInfo";
	
	static public final QName SPAWN_RESOURCES_ATTR = new QName(
			GENII_CLOUDBES_NS, SPAWN_RESOURCES_NAME);
	static public final QName RESOURCE_KILL_ATTR = new QName(
			GENII_CLOUDBES_NS, RESOURCE_KILL_NAME);
	static public final QName SHRINK_RESOURCES_ATTR = new QName(
			GENII_CLOUDBES_NS, SHRINK_RESOURCES_NAME);
	static public final QName STATUS_ATTR = new QName(
			GENII_CLOUDBES_NS, STATUS_NAME);
	static public final QName VM_STATUS_ATTR = new QName(
			GENII_CLOUDBES_NS, VM_STATUS_NAME);
	static public final QName VM_STATUS_COLLECTION_ATTR = new QName(
			GENII_CLOUDBES_NS, VM_STATUS_COLLECTION_NAME);
	static public final QName VM_INFO_ATTR = new QName(
			GENII_CLOUDBES_NS, VM_INFO_NAME);
	
}
