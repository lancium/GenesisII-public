package edu.virginia.vcgr.genii.container.q2.summary;

import java.util.Comparator;

import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.GPUArchitectureEnumeration;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType.OperatingSystemTypes;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;
import edu.virginia.vcgr.appmgr.os.GPUArchitecture;

public class HostDescription implements Comparable<HostDescription>
{
	static public Comparator<HostDescription> ALPHABETICAL_COMPARATOR = new Comparator<HostDescription>()
	{
		@Override
		public int compare(HostDescription o1, HostDescription o2)
		{
			return o1.toString().compareTo(o2.toString());
		}
	};

	private ProcessorArchitecture _arch;
	private OperatingSystemTypes _osType;
	private GPUArchitecture _gpuArch;

	public HostDescription(ProcessorArchitecture arch,  OperatingSystemTypes osType)
	{
		_arch = arch;
		_osType = osType;

	}

	public HostDescription(ProcessorArchitectureEnumeration archEnum, OperatingSystemTypeEnumeration osEnum)
	{
		this(ProcessorArchitecture.valueOf(archEnum.getValue()), OperatingSystemTypes.valueOf(osEnum.getValue()));
	}

	public boolean equals(HostDescription other)
	{
		return _arch == other._arch && _osType == other._osType;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof HostDescription)
			return equals((HostDescription) other);

		return false;
	}

	@Override
	public int hashCode()
	{
		return _arch.hashCode() ^ _osType.hashCode();
	}

	@Override
	public String toString()
	{
		return String.format("%s on %s", _osType, _arch);
	}

	@Override
	public int compareTo(HostDescription o)
	{
		return ALPHABETICAL_COMPARATOR.compare(this, o);
	}
}