package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.Serializable;

public class ResourceConstraints implements Serializable
{
	static final long serialVersionUID = 0L;

	private Double _totalPhysicalMemory = null;
	private Double _wallclockTimeLimit = null;

	final public void setTotalPhysicalMemory(Double totalPhysicalMemory)
	{
		_totalPhysicalMemory = totalPhysicalMemory;
	}

	final public void setWallclockTimeLimit(Double wallclockTimeLimit)
	{
		_wallclockTimeLimit = wallclockTimeLimit;
	}

	final public Double getTotalPhysicalMemory()
	{
		return _totalPhysicalMemory;
	}

	final public Double getWallclockTimeLimit()
	{
		return _wallclockTimeLimit;
	}
}