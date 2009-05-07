package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPersister<InformationType>
	implements InformationPersister<InformationType>
{
	private Map<InformationEndpoint, InformationResult<InformationType>> 
		_storage = new HashMap<InformationEndpoint, InformationResult<InformationType>>();
	
	@Override
	public InformationResult<InformationType> get(InformationEndpoint endpoint)
	{
		return _storage.get(endpoint);
	}

	@Override
	public void persist(InformationEndpoint endpoint,
			InformationResult<InformationType> information)
	{
		_storage.put(endpoint, information);
	}
}