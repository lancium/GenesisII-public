package edu.virginia.vcgr.genii.cloud;

import javax.xml.namespace.QName;

public interface CloudConstants{
	
	static public final String GENII_CLOUDBES_NS =
		"http://vcgr.cs.virginia.edu/genii/2010/10/bes/cloud";

	
	static public final String STATUS_NAME =
		"Status";
	static public final String SPAWN_RESOURCES_NAME =
		"SpawnResources";
	static public final String SHRINK_RESOURCES_NAME =
		"ShrinkResources";
	
	
	static public final QName SPAWN_RESOURCES_ATTR = new QName(
			GENII_CLOUDBES_NS, SPAWN_RESOURCES_NAME);
	static public final QName SHRINK_RESOURCES_ATTR = new QName(
			GENII_CLOUDBES_NS, SHRINK_RESOURCES_NAME);
	static public final QName STATUS_ATTR = new QName(
			GENII_CLOUDBES_NS, STATUS_NAME);
	
}
