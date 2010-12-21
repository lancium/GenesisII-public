package edu.virginia.vcgr.genii.container.cleanup;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

final public class CleanupContext
{
	private Map<String, Collection<CleanupReason>> _cleanupMap =
		new HashMap<String, Collection<CleanupReason>>();
	
	CleanupContext()
	{
	}
	
	final Map<String, Collection<CleanupReason>> resourcesToClean()
	{
		return _cleanupMap;
	}
	
	final public void addResource(String resourceID, 
		String cleanupReasonFormat, Object...reasonArgs)
	{
		Collection<CleanupReason> reasons = _cleanupMap.get(resourceID);
		if (reasons == null)
			_cleanupMap.put(resourceID,
				reasons = new LinkedList<CleanupReason>());
		reasons.add(new CleanupReason(String.format(
			cleanupReasonFormat, reasonArgs)));
	}
}