package edu.virginia.vcgr.genii.client.container;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public interface ContainerConstants
{
	static public final String CONTAINER_SERVICES_PROPERTIES_NS = "http://vcgr.cs.virginia.edu/genesisII/container-services/properties";

	static public final QName PROPERTY_DOWNLOAD_TMPDIR = new QName(CONTAINER_SERVICES_PROPERTIES_NS, "download-mgr-tmpdir");
	static public final QName PROPERTY_SCRATCH_SPACE_DIR = new QName(CONTAINER_SERVICES_PROPERTIES_NS, "scratch-space-dir");

	static public final QName CONTAINER_ID_METADATA_ELEMENT = new QName(GenesisIIConstants.GENESISII_NS, "container-id");
}
