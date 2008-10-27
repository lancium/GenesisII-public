package edu.virginia.vcgr.genii.client.container;

import javax.xml.namespace.QName;

public interface ContainerConstants
{
	static public final String CONTAINER_SERVICES_PROPERTIES_NS =
		"http://vcgr.cs.virginia.edu/genesisII/container-services/properties";
	
	static public final QName PROPERTY_DOWNLOAD_TMPDIR =
		new QName(CONTAINER_SERVICES_PROPERTIES_NS,
			"download-mgr-tmpdir");
	static public final QName PROPERTY_SCRATCH_SPACE_DIR =
		new QName(CONTAINER_SERVICES_PROPERTIES_NS,
			"scratch-space-dir");
}
