package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple persister which stores things in memory.
 * 
 * @author mmm2a
 *
 * @param <InformationType>
 */
public class InMemoryPersister<InformationType>
	implements InformationPersister<InformationType>
{
	private Map<InformationEndpoint, InformationResult<InformationType>> 
		_storage = new HashMap<InformationEndpoint, InformationResult<InformationType>>();
	
	/** {@inheritDoc} */
	@Override
	public InformationResult<InformationType> get(InformationEndpoint endpoint)
	{
		return _storage.get(endpoint);
	}

	/** {@inheritDoc} */
	@Override
	public void persist(InformationEndpoint endpoint,
			InformationResult<InformationType> information)
	{
		if (information == null)
			_storage.remove(endpoint);
		else
			_storage.put(endpoint, information);
	}
}