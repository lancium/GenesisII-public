package edu.virginia.vcgr.genii.client.bes;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;

public class BESConstants
{
	public final String BES_FACTORY_NS = "http://schemas.ggf.org/bes/2006/08/bes-factory";
	public final String BES_MANAGEMENT_NS = "http://schemas.ggf.org/bes/2006/08/bes-management";
	static public final String GENII_BES_NS = "http://vcgr.cs.virginia.edu/genii/2008/3/bes";
	public final String GENII_BES_NOTIFICATION_NS = "http://vcgr.cs.virginia.edu/genii/2008/03/bes/notification";

	public final String BES_FACTORY_PORT_TYPE_NAME = "BESFactoryPortType";
	public final String BES_MANAGEMENT_PORT_TYPE_NAME = "BESManagementPortType";
	public final String GENII_BES_PORT_TYPE_NAME = "GeniiBESPortType";

	public final String GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT = "Subscribe";
	public final String GENII_BES_NOTIFICATION_ACTIVITY_STATE_ELEMENT = "activity-state";

	public final PortType BES_FACTORY_PORT_TYPE()
	{
		return PortType.portTypeFactory().get(new QName(BES_FACTORY_NS, BES_FACTORY_PORT_TYPE_NAME));
	}

	public final PortType BES_MANAGEMENT_PORT_TYPE()
	{
		return PortType.portTypeFactory().get(new QName(BES_MANAGEMENT_NS, BES_MANAGEMENT_PORT_TYPE_NAME));
	}

	public final PortType GENII_BES_PORT_TYPE()
	{
		return PortType.portTypeFactory().get(new QName(GENII_BES_NS, GENII_BES_PORT_TYPE_NAME));
	}

	public final QName DEPLOYER_EPR_ATTR = new QName(GENII_BES_NS, "deployer");
	public final QName GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT_QNAME = new QName(GENII_BES_NOTIFICATION_NS,
		GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT);
	public final QName GENII_BES_NOTIFICATION_STATE_ELEMENT_QNAME = new QName(GENII_BES_NOTIFICATION_NS,
		GENII_BES_NOTIFICATION_ACTIVITY_STATE_ELEMENT);

	public final String NAMING_PROFILE_WS_ADDRESSING = "http://schemas.ggf.org/bes/2006/08/bes/naming/BasicWSAddressing";
	public final String NAMING_PROFILE_WS_NAMING = "http://www.ogf.org/naming/2006/08/naming-uwsep-pf";

	static public final String LOCAL_RESOURCE_MANAGER_TYPE_BASE = "http://vcgr.cs.virginia.edu/2008/03/resource-manager-type/";

	static public final String LOCAL_RESOURCE_MANAGER_TYPE_SIMPLE = LOCAL_RESOURCE_MANAGER_TYPE_BASE + "simple";
	static public final String LOCAL_RESOURCE_MANAGER_TYPE_PBS = LOCAL_RESOURCE_MANAGER_TYPE_BASE + "pbs";
	static public final String LOCAL_RESOURCE_MANAGER_TYPE_SGE = LOCAL_RESOURCE_MANAGER_TYPE_BASE + "sge";
	static public final String LOCAL_RESOURCE_MANAGER_TYPE_GRID_QUEUE = LOCAL_RESOURCE_MANAGER_TYPE_BASE + "grid-queue";

	public final String CONFIG_PROPERTY_WORKER_DIR = "edu.virginia.vcgr.genii.container.bes.worker-dir";
	public final String CONFIG_PROPERTY_WORKER_DIR_ALLOW_OVERRIDE =
		"edu.virginia.vcgr.genii.container.bes.worker-dir.allow-override";

	static public final String POLICY_RESOURCE_PROPERTY_NAME = "Policy";
	static public final String THRESHOLD_RESOURCE_PROPERTY_NAME = "Threshold";

	static public final QName POLICY_RP = new QName(GENII_BES_NS, POLICY_RESOURCE_PROPERTY_NAME);
	static public final QName THRESHOLD_RP = new QName(GENII_BES_NS, THRESHOLD_RESOURCE_PROPERTY_NAME);

	public final QName NAME_ATTR = new QName(GENII_BES_NS, "Name");
	public final QName TOTAL_NUMBER_OF_ACTIVITIES_ATTR = new QName(GENII_BES_NS, "TotalNumberOfActivities");
	public final QName ACTIVITY_REFERENCE_ATTR = new QName(GENII_BES_NS, "ActivityReference");
	public final QName DESCRIPTION_ATTR = new QName(GENII_BES_NS, "Description");
	public final QName OPERATING_SYSTEM_ATTR = new QName(GENII_BES_NS, "OperatingSystem");
	public final QName CPU_ARCHITECTURE_ATTR = new QName(GENII_BES_NS, "CPUArchitecture");
	public final QName CPU_COUNT_ATTR = new QName(GENII_BES_NS, "CPUCount");
	public final QName BES_POLICY_ATTR = new QName(GENII_BES_NS, POLICY_RESOURCE_PROPERTY_NAME);
	public final QName BES_THRESHOLD_ATTR = new QName(GENII_BES_NS, THRESHOLD_RESOURCE_PROPERTY_NAME);
	public final QName OGRSH_VERSIONS_ATTR = new QName(GENII_BES_NS, "OGRSHVersion");
	public final QName SUPPORTS_FUSE_ATTR = new QName(GENII_BES_NS, "SupportsFuse");

	public final QName BES_WALLCLOCK_TIMELIMIT_ATTR = new QName(GENII_BES_NS, "WallclockTimeLimit");

	public final QName SPMD_PROVIDER_ATTR = new QName(GENII_BES_NS, "SPMDProvider");

	public final QName CPU_SPEED_ATTR = new QName(GenesisIIConstants.JSDL_NS, "IndividualCPUSpeed");
	public final QName PHYSICAL_MEMORY_ATTR = new QName(GenesisIIConstants.JSDL_NS, "PhysicalMemory");
	public final QName VIRTUAL_MEMORY_ATTR = new QName(GenesisIIConstants.JSDL_NS, "VirtualMemory");

	static public final String IS_ACCEPTING_NEW_ACTIVITIES_NAME = "IsAcceptingNewActivities";
	public final QName IS_ACCEPTING_NEW_ACTIVITIES_ATTR = new QName(GENII_BES_NS, IS_ACCEPTING_NEW_ACTIVITIES_NAME);

	public final QName FILESYSTEM_SUPPORT_ATTR = new QName(GENII_BES_NS, "supported-filesystem");
}