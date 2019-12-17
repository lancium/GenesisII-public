package edu.virginia.vcgr.genii.client.jsdl.personality.common;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceConstraints implements Serializable
{
	static private Log _logger = LogFactory.getLog(ResourceConstraints.class);

	static final long serialVersionUID = 0L;

	private Double _totalPhysicalMemory = null;
	private Double _wallclockTimeLimit = null;
	private Double _totalCPUCount = null;
	private Double _GPUCountPerNode = null;
	private Double _GPUMemoryPerNode = null;
	private Boolean _ExclusiveExecution = null;

	final public void setTotalPhysicalMemory(Double totalPhysicalMemory)
	{
		_totalPhysicalMemory = totalPhysicalMemory;
	}

	final public void setWallclockTimeLimit(Double wallclockTimeLimit)
	{
		_wallclockTimeLimit = wallclockTimeLimit;
	}

	final public void setTotalCPUCount(Double totalCPUCount)
	{
		_totalCPUCount = totalCPUCount;
	}
	
	final public void setGPUCountPerNode(Double GPUCountPerNode)
	{
		_GPUCountPerNode = GPUCountPerNode;
	}
	
	final public void setGPUMemoryPerNode(Double gpuMemoryPerNode)
	{
		_GPUMemoryPerNode = gpuMemoryPerNode;
	}
	
	final public void setExclusiveExecution(Boolean exclusiveExecution)
	{
		_ExclusiveExecution = exclusiveExecution;
		_logger.info("-------EXExecution: setExclusiveExection ------" + _ExclusiveExecution);
	}

	final public Double getTotalPhysicalMemory()
	{
		return _totalPhysicalMemory;
	}

	final public Double getWallclockTimeLimit()
	{
		return _wallclockTimeLimit;
	}

	final public Double getTotalCPUCount()
	{
		return _totalCPUCount;
	}
	
	final public Double getGPUCountPerNode()
	{
		return _GPUCountPerNode;
	}
	
	final public Double getGPUMemoryPerNode()
	{
		return _GPUMemoryPerNode;
	}
	
	final public Boolean getExclusiveExecution()
	{
		_logger.info("-------EXExecution: getExclusiveExection ------" + _ExclusiveExecution);
		return _ExclusiveExecution;
	}
}
