package edu.virginia.vcgr.genii.client.bes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.utils.units.ClockSpeed;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class ResourceOverrides implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "operating-system-name", required = false)
	private OperatingSystemNames _operatingSystemName = null;

	@XmlElement(namespace = BESConstructionParameters.BES_CONS_PARMS_NS, name = "operating-system-version", required = false)
	private String _operatingSystemVersion = null;

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

	final public ProcessorArchitecture cpuArchitecture()
	{
		return _cpuArchitectureName;
	}

	final public void cpuArchitecture(ProcessorArchitecture arch)
	{
		_cpuArchitectureName = arch;
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