package edu.virginia.vcgr.genii.container.q2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.Resources_Type;

import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;

/**
 * This class matches job id's to bes id's.  It's used during the
 * scheduling phase.
 * 
 * @author mmm2a
 */
public class ResourceMatcher
{
	static private Log _logger = LogFactory.getLog(ResourceMatcher.class);
	
	static private boolean matchArch(BESInformation info, 
		CPUArchitecture_Type arch)
	{
		ProcessorArchitectureEnumeration pArch = arch.getCPUArchitectureName();
		if (pArch == null)
			return true;
		
		return info.getProcessorArchitecture().equals(pArch);
	}
	
	static private boolean matchOS(BESInformation info, 
		OperatingSystem_Type os)
	{
		OperatingSystemType_Type osType = os.getOperatingSystemType();
		if (osType != null)
		{
			OperatingSystemTypeEnumeration osTypeName = 
				osType.getOperatingSystemName();
			if (osTypeName != null && 
				!(osTypeName.equals(info.getOperatingSystemType())))
				return false;
		}
		
		String version = os.getOperatingSystemVersion();
		if (version != null && !version.equals(
			info.getOperatingSystemVersion()))
			return false;
		
		return true;
	}
	
	/**
	 * This operation indicates whether or not the given job can be
	 * run on the indicated bes container.
	 * 
	 * @param jobID The job to match against.
	 * @param besID The bes to match against.
	 * @return
	 */
	public boolean matches(JobDefinition_Type jsdl, BESInformation besInfo)
	{
		if (jsdl == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- no jsdl given.");
			return false;
		}
		
		if (besInfo == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- " +
				"no bes information available.");
			return false;
		}
		
		JobDescription_Type desc = jsdl.getJobDescription();
		if (desc == null)
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- invalid jsdl given.");
			return false;
		}
		
		Resources_Type resources = desc.getResources();
		if (resources == null)
		{
			_logger.debug(
				"Matching jsdl to bes information -- no restrictions given.");
			return true;
		}
		
		CPUArchitecture_Type arch = resources.getCPUArchitecture();
		if (arch != null && !matchArch(besInfo, arch))
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- Processor architectures don't match.");
			return false;
		}
		
		OperatingSystem_Type osType = resources.getOperatingSystem();
		if (osType != null && !matchOS(besInfo, osType))
		{
			_logger.warn(
				"Cannot match jsdl to bes information -- OS restrictinos don't match.");
			return false;
		}
		
		return true;
	}
}