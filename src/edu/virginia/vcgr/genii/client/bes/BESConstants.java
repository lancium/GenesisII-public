package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.PortType;

public interface BESConstants
{
	static public final String BES_FACTORY_NS =
		"http://schemas.ggf.org/bes/2006/08/bes-factory";
	static public final String BES_MANAGEMENT_NS =
		"http://schemas.ggf.org/bes/2006/08/bes-management";
	static public final String GENII_BES_NS =
		"http://vcgr.cs.virginia.edu/genii/2008/3/bes";
	
	static public final String BES_FACTORY_PORT_TYPE_NAME =
		"BESFactoryPortType";
	static public final String BES_MANAGEMENT_PORT_TYPE_NAME =
		"BESManagementPortType";
	static public final String GENII_BES_PORT_TYPE_NAME =
		"GeniiBESPortType";
	
	static public final PortType BES_FACTORY_PORT_TYPE =
		PortType.get(new QName(BES_FACTORY_NS, BES_FACTORY_PORT_TYPE_NAME));
	static public final PortType BES_MANAGEMENT_PORT_TYPE =
		PortType.get(new QName(BES_MANAGEMENT_NS, BES_MANAGEMENT_PORT_TYPE_NAME));
	static public final PortType GENII_BES_PORT_TYPE =
		PortType.get(new QName(GENII_BES_NS, GENII_BES_PORT_TYPE_NAME));
	
	static public final QName DEPLOYER_EPR_ATTR =
		new QName(GENII_BES_NS, "deployer");
	
	static public final String NAMING_PROFILE_WS_ADDRESSING = 
		"http://schemas.ggf.org/bes/2006/08/bes/naming/BasicWSAddressing";
	static public final String NAMING_PROFILE_WS_NAMING =
		"http://www.ogf.org/naming/2006/08/naming-uwsep-pf";
	
	static public final String LOCAL_RESOURCE_MANAGER_TYPE_SIMPLE =
		"http://vcgr.cs.virginia.edu/2008/03/resource-manager-type/simple";
	
	static public final String CONFIG_PROPERTY_WORKER_DIR =
		"edu.virginia.vcgr.genii.container.bes.worker-dir";
	static public final String CONFIG_PROPERTY_WORKER_DIR_ALLOW_OVERRIDE =
		"edu.virginia.vcgr.genii.container.bes.worker-dir.allow-override";
}