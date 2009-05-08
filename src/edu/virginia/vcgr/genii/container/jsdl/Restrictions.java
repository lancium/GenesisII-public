package edu.virginia.vcgr.genii.container.jsdl;

import org.apache.commons.logging.Log;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.mortbay.log.LogFactory;

public class Restrictions
{
	static private Log _logger = LogFactory.getLog(Restrictions.class);
	
	private ProcessorArchitectureEnumeration _arch = null;
	private OperatingSystemTypeEnumeration _osType = null;
	private String _osVersion = null;
	
	public void setProcessorArchitectureRestriction(
		ProcessorArchitectureEnumeration arch)
	{
		_logger.trace(String.format(
			"Setting Processor Arch restriction to %s.",
			arch));
		
		_arch = arch;
	}
	
	public ProcessorArchitectureEnumeration 
		getProcessorArchitectureRestriction()
	{
		return _arch;
	}
	
	public void setOperatingSystemTypeRestriction(
		OperatingSystemTypeEnumeration osType)
	{
		_logger.trace(String.format(
			"Setting OS Type restriction to %s.",
			osType));
			
		_osType = osType;
	}
	
	public OperatingSystemTypeEnumeration
		getOperatingSystemTypeRestriction()
	{
		return _osType;
	}
	
	public void setOperatingSystemVersionRestriction(String osVersion)
	{
		_logger.trace(String.format(
			"Setting OS Version restriction to %s.",
			osVersion));
			
		_osVersion = osVersion;
	}
	
	public String getOperatingSystemVersionRestriction() 
	{
		return _osVersion;
	}
}