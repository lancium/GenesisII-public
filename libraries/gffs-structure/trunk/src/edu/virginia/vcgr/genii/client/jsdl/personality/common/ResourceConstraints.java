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
		_logger.info("---JSDL:---- in ResourceConstraints setTotalGPUCount----------------" + GPUCountPerNode);
	}
	
	final public void setGPUMemoryPerNode(Double gpuMemoryPerNode)
	{
		_GPUMemoryPerNode = gpuMemoryPerNode;
		_logger.info("---JSDL:---- in ResourceConstraints setGPUMemory----------------" + gpuMemoryPerNode);
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
		_logger.info("---JSDL:---- in ResourceConstraints getTotalGPUCount----------------" + _GPUCountPerNode);
		return _GPUCountPerNode;
	}
	
	final public Double getGPUMemoryPerNode()
	{
		_logger.info("---JSDL:---- in ResourceConstraints getGPUMemoryPerNode----------------" + _GPUMemoryPerNode);
		return _GPUMemoryPerNode;
	}
}
