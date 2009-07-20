package edu.virginia.vcgr.genii.client.nativeq;

public interface NativeQConstants
{
	static final public String BASE_PROPERTY_STRING =
		"edu.virginia.vcgr.genii.native-q.resource-override.";
	
	static final public String OPERATING_SYSTEM_NAME_PROPERTY =
		BASE_PROPERTY_STRING + "operating-system-name";
	static final public String OPERATING_SYSTEM_VERSION_PROPERTY =
		BASE_PROPERTY_STRING + "operating-system-version";
	static final public String CPU_ARCHITECTURE_NAME_PROPERTY =
		BASE_PROPERTY_STRING + "cpu-architecture-name";
	static final public String CPU_COUNT_PROPERTY =
		BASE_PROPERTY_STRING + "cpu-count";
	static final public String CPU_SPEED_PROPERTY =
		BASE_PROPERTY_STRING + "cpu-speed";
	static final public String PHYSICAL_MEMORY_PROPERTY =
		BASE_PROPERTY_STRING + "physical-memory";
	static final public String VIRTUAL_MEMORY_PROPERTY =
		BASE_PROPERTY_STRING + "virtual-memory";
}