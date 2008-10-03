package edu.virginia.vcgr.genii.client.spmd;

public abstract class AbstractSPMDTranslatorFactory 
	implements SPMDTranslatorFactory
{
	private String _providerName;
	
	protected AbstractSPMDTranslatorFactory(String providerName)
	{
		_providerName = providerName;
	}
	
	@Override
	public String getProviderName()
	{
		return _providerName;
	}
}