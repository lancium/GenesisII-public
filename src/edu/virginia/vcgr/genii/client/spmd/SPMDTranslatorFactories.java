package edu.virginia.vcgr.genii.client.spmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

public class SPMDTranslatorFactories
{
	static private ServiceLoader<SPMDTranslatorFactory> _factories =
		ServiceLoader.load(SPMDTranslatorFactory.class);
	
	static public SPMDTranslatorFactory getSPMDTranslatorFactory(
		String providerName) throws SPMDException
	{
		for (SPMDTranslatorFactory factory : _factories)
		{
			if (factory.getProviderName().equals(providerName))
				return factory;
		}
		
		throw new SPMDException(
			"Unable to find SPMD Translator Factory provider \"" 
			+ providerName + "\".");
	}
	
	static public Collection<String> listSPMDTranslatorFactories()
	{
		Collection<String> ret = new ArrayList<String>();
		
		for (SPMDTranslatorFactory factory : _factories)
		{
			ret.add(factory.getProviderName());
		}
		
		return ret;
	}
}