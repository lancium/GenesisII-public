package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

public class JobUnderstandingContext
{
	private String _requiredOGRSHVersion;
	private Double _totalPhyscialMemory;
	
	public JobUnderstandingContext(String requiredOGRSHVersion, Double totalPhyscialMemory)
	{
		_requiredOGRSHVersion = requiredOGRSHVersion; 
		_totalPhyscialMemory = totalPhyscialMemory;
	}
	
	public Double getTotalPhysicalMemory()
	{
		return _totalPhyscialMemory;
	}
	
	public String getRequiredOGRSHVersion()
	{
		return _requiredOGRSHVersion;
	}
	
}