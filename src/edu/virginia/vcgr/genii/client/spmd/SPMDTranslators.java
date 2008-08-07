package edu.virginia.vcgr.genii.client.spmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

public class SPMDTranslators
{
	static private ServiceLoader<SPMDTranslator> _translators =
		ServiceLoader.load(SPMDTranslator.class);
	
	static public SPMDTranslator getSPMDTranslator(String providerName)
		throws SPMDException
	{
		for (SPMDTranslator translator : _translators)
		{
			if (translator.getProviderName().equals(providerName))
				return translator;
		}
		
		throw new SPMDException("Unable to find SPMD Translator provider \"" 
			+ providerName + "\".");
	}
	
	static public Collection<String> listSPMDTranslators()
	{
		Collection<String> ret = new ArrayList<String>();
		
		for (SPMDTranslator translator : _translators)
		{
			ret.add(translator.getProviderName());
		}
		
		return ret;
	}
}