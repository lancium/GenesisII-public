package edu.virginia.vcgr.genii.client.spmd;

public abstract class AbstractSPMDTranslator implements SPMDTranslator
{
	private String _providerName;
	
	protected AbstractSPMDTranslator(String providerName)
	{
		_providerName = providerName;
	}
	
	@Override
	public String getProviderName()
	{
		return _providerName;
	}
}