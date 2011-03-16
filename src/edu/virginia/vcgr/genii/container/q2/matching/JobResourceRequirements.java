package edu.virginia.vcgr.genii.container.q2.matching;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.Boundary_Type;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.FileSystem_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Resources_Type;

import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;

public class JobResourceRequirements
{
	static private Log _logger = LogFactory.getLog(JobResourceRequirements.class);
	
	static private void appendParameter(StringBuilder parameters,
		Object parameter)
	{
		if (parameter == null)
			return;
		
		if (parameters.length() > 0)
			parameters.append(", ");
		parameters.append(parameter);
	}
	
	static private void appendParameter(StringBuilder parameters, 
		String title, Object value)
	{
		if (value != null)
			appendParameter(parameters,
				String.format("%s = %s", title, value));
	}
	
	private ProcessorArchitectureEnumeration _arch = null;
	private OperatingSystemTypeEnumeration _osType = null;
	private String _osVersion = null;
	private Double _memoryRequirement = null;
	private Double _wallclockTimeLimit = null;
	private MatchingParameters _matchingParameters = null;
	private Set<String> _requestedFilesystems = new HashSet<String>();
	
	private void fillInArchInformation(CPUArchitecture_Type arch)
	{
		ProcessorArchitectureEnumeration pArch = arch.getCPUArchitectureName();
		if (pArch != null)
			_arch = pArch;
	}
	
	private void fillInOsInformation(OperatingSystem_Type os)
	{
		OperatingSystemType_Type osType = os.getOperatingSystemType();
		if (osType != null)
		{
			OperatingSystemTypeEnumeration osTypeName = 
				osType.getOperatingSystemName();
			if (osTypeName != null)
				_osType = osTypeName;
		}
		
		String version = os.getOperatingSystemVersion();
		if (version != null)
			_osVersion = version;
	}
	
	private void fillInMemoryInformation(RangeValue_Type memRange)
	{
		Boundary_Type memUpperBound = memRange.getUpperBoundedRange();
		if (memUpperBound != null)
			_memoryRequirement = new Double(memUpperBound.get_value());
	}
	
	private void fillInWallclockInformation(RangeValue_Type wallRange)
	{
		Boundary_Type wallUpperBound = wallRange.getUpperBoundedRange();
		if (wallUpperBound != null)
			_wallclockTimeLimit = new Double(wallUpperBound.get_value());
	}
	
	public JobResourceRequirements()
	{
		this(null);
	}
	
	public JobResourceRequirements(JobDefinition_Type jsdl)
	{
		_matchingParameters = new MatchingParameters();
		
		if (jsdl == null)
			return;
		
		JobDescription_Type desc = jsdl.getJobDescription();
		if (desc == null)
			return;
		
		Resources_Type resources = desc.getResources();
		if (resources == null)
			return;
		
		CPUArchitecture_Type arch = resources.getCPUArchitecture();
		if (arch != null)
			fillInArchInformation(arch);
		
		OperatingSystem_Type osType = resources.getOperatingSystem();
		if (osType != null)
			fillInOsInformation(osType);
		
		RangeValue_Type memoryRange = resources.getTotalPhysicalMemory();
		if (memoryRange != null)
			fillInMemoryInformation(memoryRange);
		
		MessageElement []resourcesAny = resources.get_any();
		if (resourcesAny != null)
		{
			for (MessageElement a : resourcesAny)
			{
				if (a.getQName().equals(new QName("http://vcgr.cs.virginia.edu/jsdl/genii", "WallclockTime")))
				{
					try
					{
						RangeValue_Type rType = ObjectDeserializer.toObject(a, RangeValue_Type.class);
						fillInWallclockInformation(rType);
					}
					catch (Throwable cause)
					{
						throw new RuntimeException("Unable to parse wallclock time.", cause);
					}
				}
			}
		}
		
		_matchingParameters = new MatchingParameters(MatchingParameter.matchingParameters(
			resources.get_any(), true));
		
		FileSystem_Type []fss = resources.getFileSystem();
		if (fss != null)
		{
			for (FileSystem_Type fs : fss)
			{
				_requestedFilesystems.add(fs.getName().toString());
			}
		}
	}
	
	/*
	private ProcessorArchitectureEnumeration _arch = null;
	private OperatingSystemTypeEnumeration _osType = null;
	private String _osVersion = null;
	private Double _memoryRequirement = null;
	private MatchingParameter []_matchingParameters = null;
	*/
	
	@Override
	public int hashCode()
	{
		int ret = 0x0;
		
		if (_arch != null)
			ret ^= _arch.hashCode();
		
		if (_osType != null)
			ret ^= _osType.hashCode();
		
		if (_osVersion != null)
			ret ^= _osVersion.hashCode();
		
		if (_memoryRequirement != null)
			ret ^= _memoryRequirement.hashCode();
		
		if (_matchingParameters != null)
		{
			for (MatchingParameter param : _matchingParameters.getParameters())
				ret ^= param.hashCode();
		}
		
		ret ^= _requestedFilesystems.hashCode();
		
		return ret;
	}
	
	private boolean equalsWithNulls(Object one, Object two)
	{
		if (one == null && two == null)
			return true;
		if (one == null || two == null)
			return false;
		
		return one.equals(two);
	}
	
	public boolean equals(JobResourceRequirements other)
	{
		if (!equalsWithNulls(_arch, other._arch))
			return false;
		
		if (!equalsWithNulls(_osType, other._osType))
			return false;
		
		if (!equalsWithNulls(_osVersion, other._osVersion))
			return false;
		
		if (!equalsWithNulls(_memoryRequirement, other._memoryRequirement))
			return false;
		
		if (!equalsWithNulls(_wallclockTimeLimit, other._wallclockTimeLimit))
			return false;
		
		if (_matchingParameters == null && other._matchingParameters == null)
			return true;
		if (_matchingParameters == null || other._matchingParameters == null)
			return false;
		
		for (MatchingParameter mp : other._matchingParameters.getParameters())
		{
			boolean found = false;
			for (MatchingParameter mp2 : _matchingParameters.getParameters())
			{
				if (mp.equals(mp2))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
				return false;
		}
		
		return equalsWithNulls(_requestedFilesystems, other._requestedFilesystems);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof JobResourceRequirements)
			return equals((JobResourceRequirements)other);
		
		return false;
	}
	
	public boolean matches(BESInformation besInfo)
	{
		if (besInfo == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- " +
				"no bes information available.");
			return false;
		}
		
		if (_arch != null)
		{
			ProcessorArchitectureEnumeration bArch = 
				besInfo.getProcessorArchitecture();
			if (bArch == null)
			{
				_logger.warn(
					"BES does not have a processor architecture.");
				return false;
			}

			if (!_arch.equals(bArch))
				return false;
		}
		
		if (_osType != null)
		{
			OperatingSystemTypeEnumeration bOsType = 
				besInfo.getOperatingSystemType();
			if (bOsType == null)
			{
				_logger.warn(
					"BES does not have an operating system type.");
				return false;
			}
			
			if (!_osType.equals(bOsType))
				return false;
		}
		
		if (_osVersion != null)
		{
			String bOsVersion = besInfo.getOperatingSystemVersion();
			if (bOsVersion == null)
			{
				_logger.warn(
					"BES does not have an operating system version.");
				return false;
			}
			
			if (!_osVersion.equals(bOsVersion))
				return false;
		}
		
		if (_memoryRequirement != null)
		{
			Double bMemory = besInfo.getPhysicalMemory();
			if (bMemory == null)
			{
				_logger.warn(
					"BES does not have a physical memory attribute.");
				return false;
			}
			
			if (_memoryRequirement.doubleValue() > bMemory.doubleValue())
				return false;
		}
		
		if (_wallclockTimeLimit != null)
		{
			Double wall = besInfo.getWallclockTimeLimit();
			if (wall != null)
			{
				if (_wallclockTimeLimit.doubleValue() > wall.doubleValue())
					return false;
			}
		}
		
		if (!MatchingParameter.matches(besInfo.getMatchingParameters(), _matchingParameters))
			return false;
		
		for (String reqFs : _requestedFilesystems)
		{
			if (!besInfo.supportsFilesystems(reqFs))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder parameters = new StringBuilder();
		appendParameter(parameters, "OS Type", _osType);
		appendParameter(parameters, "OS Version", _osVersion);
		appendParameter(parameters, "Processor Arch", _arch);
		appendParameter(parameters, "Memory Requirement", _memoryRequirement);
		if (_matchingParameters != null)
		{
			for (MatchingParameter parameter : _matchingParameters.getParameters())
				appendParameter(parameters, parameter);
		}
		
		appendParameter(parameters, "Requested Filesystems", _requestedFilesystems);
		
		return String.format("Job Resource Requirements(%s)", parameters);
	}
}