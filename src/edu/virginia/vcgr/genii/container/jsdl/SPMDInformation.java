package edu.virginia.vcgr.genii.container.jsdl;

import java.io.Serializable;

public class SPMDInformation implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _spmdVariation;
	private int _numberOfProcesses;
	
	public SPMDInformation(String spmdVariation,
		int numberOfProcesses)
	{
		if (spmdVariation == null)
			throw new IllegalArgumentException(
				"SPMDVariation cannot be null.");
		
		_spmdVariation = spmdVariation;
		_numberOfProcesses = numberOfProcesses;
	}
	
	final public String getSPMDVariation()
	{
		return _spmdVariation;
	}
	
	final public int getNumberOfProcesses()
	{
		return _numberOfProcesses;
	}
	
	@Override
	public String toString()
	{
		return String.format("[%d] %s", _numberOfProcesses, _spmdVariation);
	}
}