package edu.virginia.vcgr.genii.client.jsdl;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.GPUArchitectureEnumeration;

public class Restrictions implements Serializable
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(Restrictions.class);

	private ProcessorArchitectureEnumeration _arch = null;
	private GPUArchitectureEnumeration _gpuArch = null;
	private OperatingSystemTypeEnumeration _osType = null;
	private String _osVersion = null;
	private Boolean _exclusiveExecution = null;

	public void setProcessorArchitectureRestriction(ProcessorArchitectureEnumeration arch)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("Setting Processor Arch restriction to %s.", arch));

		_arch = arch;
	}
	
	public void setExclusiveExecutionRestriction(Boolean exclusiveExecution)
	{
		_logger.info("---EXExecution:------ returning " + _exclusiveExecution);
		_exclusiveExecution = exclusiveExecution;
	}

	public ProcessorArchitectureEnumeration getProcessorArchitectureRestriction()
	{
		return _arch;
	}

	public void setGPUArchitectureRestriction(GPUArchitectureEnumeration gpuArch)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("Setting GPU Arch restriction to %s.", gpuArch));

		_gpuArch = gpuArch;
	}

	public GPUArchitectureEnumeration getGPUArchitectureRestriction()
	{
		return _gpuArch;
	}

	public void setOperatingSystemTypeRestriction(OperatingSystemTypeEnumeration osType)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("Setting OS Type restriction to %s.", osType));

		_osType = osType;
	}

	public OperatingSystemTypeEnumeration getOperatingSystemTypeRestriction()
	{
		return _osType;
	}

	public void setOperatingSystemVersionRestriction(String osVersion)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("Setting OS Version restriction to %s.", osVersion));

		_osVersion = osVersion;
	}

	public String getOperatingSystemVersionRestriction()
	{
		return _osVersion;
	}
}
