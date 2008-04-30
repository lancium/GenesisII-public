package edu.virginia.vcgr.genii.client.nativeq;

import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;

public class BasicResourceAttributes extends ResourceAttributes
{
	private OperatingSystem_Type _operatingSystem;
    private CPUArchitecture_Type _CPUArchitecture;

    private Integer _cpuCount;
    private Double _CPUSpeed;
    private Long _physicalMemory;
    private Long _virtualMemory;
	
    public BasicResourceAttributes(OperatingSystem_Type operatingSystem,
		CPUArchitecture_Type architecture, Integer cpuCount, Double cpuSpeed,
		Long physicalMemory, Long virtualMemory)
	{
		_operatingSystem = operatingSystem;
		_CPUArchitecture = architecture;
		_cpuCount = cpuCount;
		_CPUSpeed = cpuSpeed;
		_physicalMemory = physicalMemory;
		_virtualMemory = virtualMemory;
	}

	public OperatingSystem_Type getOperatingSystem()
	{
		return _operatingSystem;
	}

	public CPUArchitecture_Type getCPUArchitecture()
	{
		return _CPUArchitecture;
	}

	public Integer getCPUCount()
	{
		return _cpuCount;
	}

	public Double getCPUSpeed()
	{
		return _CPUSpeed;
	}

	public Long getPhysicalMemory()
	{
		return _physicalMemory;
	}

	public Long getVirtualMemory()
	{
		return _virtualMemory;
	}
	
	protected void describe(StringBuilder builder, String tabPrefix)
	{
		if (_operatingSystem != null)
			addDescription(builder.append(tabPrefix), _operatingSystem);
		if (_CPUArchitecture != null)
			addDescription(builder.append(tabPrefix), _CPUArchitecture);
		if (_cpuCount != null)
			addDescription(builder.append(tabPrefix), "CPU Count", 
				_cpuCount, "");
		if (_CPUSpeed != null)
			addDescription(builder.append(tabPrefix), "CPU Speed", 
				_CPUSpeed, "Hz");
		if (_physicalMemory != null)
			addDescription(builder.append(tabPrefix), "Physical Memory", 
				_physicalMemory, "Bytes");
		if (_virtualMemory != null)
			addDescription(builder.append(tabPrefix), "Virtual Memory", 
				_virtualMemory, "Bytes");
	}
	
	static private void addDescription(StringBuilder builder, 
		OperatingSystem_Type os)
	{
		OperatingSystemType_Type osType = os.getOperatingSystemType();
		String version = os.getOperatingSystemVersion();
		OperatingSystemTypeEnumeration osName = null;
		

		if (osType != null)
			osName = osType.getOperatingSystemName();
		
		if (osName == null)
			osName = OperatingSystemTypeEnumeration.Unknown;
		
		if (version == null)
			version = "<unknown>";
		
		builder.append(String.format("Operating System: %s version %s\n", 
			osName.getValue(), version));
	}
	
	static private void addDescription(StringBuilder builder, CPUArchitecture_Type arch)
	{
		ProcessorArchitectureEnumeration pArch = arch.getCPUArchitectureName();
		if (pArch == null)
			pArch = ProcessorArchitectureEnumeration.other;
		
		builder.append(String.format("CPU Arch:  %s\n", pArch.getValue()));
	}
	
	static private void addDescription(StringBuilder builder, String header, 
		Number number, String units)
	{
		builder.append(String.format("%s:  %s %s\n", header, number, units));
	}
}