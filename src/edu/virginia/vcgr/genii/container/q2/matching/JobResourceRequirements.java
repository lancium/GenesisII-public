package edu.virginia.vcgr.genii.container.q2.matching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.Boundary_Type;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Resources_Type;

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
	private MatchingParameter []_matchingParameters = null;
	
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
	
	public JobResourceRequirements()
	{
		this(null);
	}
	
	public JobResourceRequirements(JobDefinition_Type jsdl)
	{
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
		
		_matchingParameters = MatchingParameter.matchingParameters(
			resources.get_any());
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
		
		if (_matchingParameters != null)
		{
			for (MatchingParameter parameter : _matchingParameters)
			{
				if (!parameter.matches(besInfo.getMatchingParameters()))
					return false;
			}
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
			for (MatchingParameter parameter : _matchingParameters)
				appendParameter(parameters, parameter);
		}
		
		return String.format("Job Resource Requirements(%s)", parameters);
	}
}