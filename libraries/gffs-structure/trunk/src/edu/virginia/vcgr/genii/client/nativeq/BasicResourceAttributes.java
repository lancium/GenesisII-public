package edu.virginia.vcgr.genii.client.nativeq;

import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.GPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.GPUArchitectureEnumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BasicResourceAttributes extends ResourceAttributes
{

	static private Log _logger = LogFactory.getLog(BasicResourceAttributes.class);
	private OperatingSystem_Type _operatingSystem;
	private CPUArchitecture_Type _CPUArchitecture;
	private GPUArchitecture_Type _GPUArchitecture;

	private Integer _gpuCount;
	private Integer _cpuCount;
	private Double _CPUSpeed;
	private Long _physicalMemory;
	private Long _virtualMemory;

	public BasicResourceAttributes(OperatingSystem_Type operatingSystem, CPUArchitecture_Type architecture, Integer cpuCount, Double cpuSpeed,
		Long physicalMemory, Long virtualMemory, GPUArchitecture_Type gpuArchitecture, Integer gpuCount)
	{
		_operatingSystem = operatingSystem;
		_CPUArchitecture = architecture;
		_gpuCount = gpuCount;
		_cpuCount = cpuCount;
		_CPUSpeed = cpuSpeed;
		_physicalMemory = physicalMemory;
		_virtualMemory = virtualMemory;
		_GPUArchitecture = gpuArchitecture;
	}

	public OperatingSystem_Type getOperatingSystem()
	{
		return _operatingSystem;
	}

	public CPUArchitecture_Type getCPUArchitecture()
	{
		_logger.info("---JSDL: in BasicResourceAttributes, returning cpuArchitecture: " + _CPUArchitecture);
		return _CPUArchitecture;
	}

	public GPUArchitecture_Type getGPUArchitecture()
	{
		_logger.info("---JSDL: in BasicResourceAttributes, returning gpuArchitecture: " + _GPUArchitecture);
		return _GPUArchitecture;
	}

	public Integer getGPUCount()
    {
		return _gpuCount;
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
		if (_GPUArchitecture != null)
			addDescription(builder.append(tabPrefix), _GPUArchitecture);
		if (_gpuCount != null)
			addDescription(builder.append(tabPrefix), "GPU Count", _gpuCount, "");
		if (_cpuCount != null)
			addDescription(builder.append(tabPrefix), "CPU Count", _cpuCount, "");
		if (_CPUSpeed != null)
			addDescription(builder.append(tabPrefix), "CPU Speed", _CPUSpeed, "Hz");
		if (_physicalMemory != null)
			addDescription(builder.append(tabPrefix), "Physical Memory", _physicalMemory, "Bytes");
		if (_virtualMemory != null)
			addDescription(builder.append(tabPrefix), "Virtual Memory", _virtualMemory, "Bytes");
	}

	static private void addDescription(StringBuilder builder, OperatingSystem_Type os)
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

		builder.append(String.format("Operating System: %s version %s\n", osName.getValue(), version));
	}

	static private void addDescription(StringBuilder builder, CPUArchitecture_Type arch)
	{
		ProcessorArchitectureEnumeration pArch = arch.getCPUArchitectureName();
		if (pArch == null)
			pArch = ProcessorArchitectureEnumeration.other;

		builder.append(String.format("CPU Arch:  %s\n", pArch.getValue()));
	}

	static private void addDescription(StringBuilder builder, GPUArchitecture_Type arch)
	{
		GPUArchitectureEnumeration gArch = arch.getGPUArchitectureName();
		if (gArch == null)
			gArch = GPUArchitectureEnumeration.other;

		builder.append(String.format("GPU Arch:  %s\n", gArch.getValue()));
	}

	static private void addDescription(StringBuilder builder, String header, Number number, String units)
	{
		builder.append(String.format("%s:  %s %s\n", header, number, units));
	}
}
