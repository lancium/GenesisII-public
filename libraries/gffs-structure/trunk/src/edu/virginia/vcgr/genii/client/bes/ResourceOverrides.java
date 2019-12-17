package edu.virginia.vcgr.genii.client.bes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.utils.units.ClockSpeed;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;
import edu.virginia.vcgr.jsdl.GPUProcessorArchitecture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceOverrides implements Serializable
{

	static private Log _logger = LogFactory.getLog(ResourceOverrides.class);
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "operating-system-name", required = false)
	private OperatingSystemNames _operatingSystemName = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "operating-system-version", required = false)
	private String _operatingSystemVersion = null;
	
	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "exclusive-execution", required = false)
	private Boolean _exclusiveExecution = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "cpu-architecture-name", required = false)
	private ProcessorArchitecture _cpuArchitectureName = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "cpu-count", required = false)
	private Integer _cpuCount = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "cpu-speed", required = false)
	private ClockSpeed _cpuSpeed = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "physical-memory", required = false)
	private Size _physicalMemory = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "virtual-memory", required = false)
	private Size _virtualMemory = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "wallclock-time-limit", required = false)
	private String _wallclockTimeLimit = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "gpu-architecture-name", required = false)
	private GPUProcessorArchitecture _gpuArchitectureName = null;
	
	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "gpu-count-per-node", required = false)
	private Integer _gpuCount = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "gpu-memory-per-node", required = false)
	private Size _gpuMemoryPerNode = null;

	final public OperatingSystemNames operatingSystemName()
	{
		return _operatingSystemName;
	}

	final public void operatingSystemName(OperatingSystemNames name)
	{
		_operatingSystemName = name;
	}

	final public String operatingSystemVersion()
	{
		return _operatingSystemVersion;
	}

	final public void operatingSystemVersion(String version)
	{
		_operatingSystemVersion = version;
	}
	
	final public Boolean exclusiveExecution()
	{
		return _exclusiveExecution;
	}
	
	final public void exclusiveExecution(Boolean exclusiveExecution)
	{
		_exclusiveExecution = exclusiveExecution;
	}

	final public ProcessorArchitecture cpuArchitecture()
	{
		return _cpuArchitectureName;
	}

	final public void cpuArchitecture(ProcessorArchitecture arch)
	{
		_cpuArchitectureName = arch;
	}

	final public GPUProcessorArchitecture gpuArchitecture()
	{
		return _gpuArchitectureName;
	}

	final public void gpuArchitecture(GPUProcessorArchitecture arch)
	{
		_gpuArchitectureName = arch;
	}
	
	final public Integer gpuCount()
	{
		return _gpuCount;
	}

	final public void gpuCount(Integer count)
	{
		_gpuCount = count;
	}

	final public Size gpuMemoryPerNode()
	{
		return _gpuMemoryPerNode;
	}

	final public void gpuMemoryPerNode(Size mem)
	{
		_gpuMemoryPerNode = mem;
	}

	final public Integer cpuCount()
	{
		return _cpuCount;
	}

	final public void cpuCount(Integer count)
	{
		_cpuCount = count;
	}

	final public ClockSpeed cpuSpeed()
	{
		return _cpuSpeed;
	}

	final public void cpuSpeed(ClockSpeed speed)
	{
		_cpuSpeed = speed;
	}

	final public Size physicalMemory()
	{
		return _physicalMemory;
	}

	final public void physicalMemory(Size mem)
	{
		_physicalMemory = mem;
	}

	final public Size virtualMemory()
	{
		return _virtualMemory;
	}

	final public void virtualMemory(Size mem)
	{
		_virtualMemory = mem;
	}

	final public Duration wallclockTimeLimit()
	{
		if (_wallclockTimeLimit == null)
			return null;

		return new Duration(_wallclockTimeLimit);
	}

	final public void wallclockTimeLimit(Duration newValue)
	{
		_wallclockTimeLimit = newValue == null ? null : newValue.toString();
	}
}
