package edu.virginia.vcgr.appmgr.patch;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;

public class PatchRestrictions
{
	private OperatingSystemType _osType = null;
	private String _osVersion = null;
	private ProcessorArchitecture _processorArch = null;
	private HostRestriction _hostRestriction = null;

	public boolean satisfies()
	{
		if (_osType != null && _osType != OperatingSystemType.getCurrent())
			return false;
		if (_osVersion != null && !_osVersion.equalsIgnoreCase(OperatingSystemType.getCurrentVersion()))
			return false;
		if (_processorArch != null && _processorArch != ProcessorArchitecture.getCurrent())
			return false;
		if (_hostRestriction != null && !_hostRestriction.satisfies())
			return false;

		return true;
	}

	public OperatingSystemType getOSType()
	{
		return _osType;
	}

	public void setOperatingSystemTypeRestriction(OperatingSystemType osType)
	{
		_osType = osType;
	}

	public String getOSVersion()
	{
		return _osVersion;
	}

	public void setOperatingSystemVersionRestriction(String osVersion)
	{
		_osVersion = osVersion;
	}

	public ProcessorArchitecture getProcessorArchitecture()
	{
		return _processorArch;
	}

	public void setProcessorArchitectureRestriction(ProcessorArchitecture processorArch)
	{
		_processorArch = processorArch;
	}

	public HostRestriction getHostRestriction()
	{
		return _hostRestriction;
	}

	public void setHostRestriction(HostRestriction hostRestriction)
	{
		_hostRestriction = hostRestriction;
	}

	@Override
	public String toString()
	{
		return String.format("Operating System:  %s\n", _osType) + String.format("Operating System Version:  %s\n", _osVersion)
			+ String.format("CPU Architecture:  %s\n", _processorArch)
			+ String.format("Hostname Restriction:  %s", _hostRestriction);

	}
}