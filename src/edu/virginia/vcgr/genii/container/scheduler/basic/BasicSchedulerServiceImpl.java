package edu.virginia.vcgr.genii.container.scheduler.basic;

import java.rmi.RemoteException;
import java.util.Collection;

import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.RNSServiceImpl;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.scheduler.NoMatchingScheduleFaultType;
import edu.virginia.vcgr.genii.scheduler.ScheduleCriteriaType;
import edu.virginia.vcgr.genii.scheduler.basic.BasicSchedulerPortType;

public class BasicSchedulerServiceImpl extends RNSServiceImpl implements
		BasicSchedulerPortType
{
	static private final String _NEXT_PROPERTY_NAME = 
		"edu.virginia.vcgr.genii.container.scheduler.basic.next-resource";
	
	public BasicSchedulerServiceImpl() throws RemoteException
	{
		super("BasicSchedulerPortType");
		
		addImplementedPortType(WellKnownPortTypes.SCHEDULER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.BASIC_SCHEDULER_PORT_TYPE);
	}

	@RWXMapping(RWXCategory.INHERITED)
	public void notify(Notify notify) throws RemoteException,
			ResourceUnknownFaultType
	{
		// For right now, we just swallow notifications up.
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public EndpointReferenceType[] scheduleActivities(
		ScheduleCriteriaType[] scheduleActivitiesRequest) 
			throws RemoteException, NoMatchingScheduleFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
    	IBasicSchedulerResource resource = (IBasicSchedulerResource)rKey.dereference();
	    Collection<InternalEntry> entries = resource.retrieveEntries(".*");
	    int choicesSize = entries.size();
	    InternalEntry []entriesA = new InternalEntry[choicesSize];
	    entries.toArray(entriesA);
	    
	    Integer next = (Integer)resource.getProperty(_NEXT_PROPERTY_NAME);
	    if (next == null)
	    	next = new Integer(0);
	    
	    if (choicesSize == 0)
	    	throw FaultManipulator.fillInFault(new NoMatchingScheduleFaultType());
	    
	    EndpointReferenceType []ret = 
	    	new EndpointReferenceType[scheduleActivitiesRequest.length];
	    
	    int lcv = 0;
	    for (ScheduleCriteriaType criteria : scheduleActivitiesRequest)
		{
			// We don't really check criteria right now, will have to eventually do
			// this.
	    	if (criteria == null && criteria != null)
	    		throw new RuntimeException();
	    	
			if (next.intValue() >= entries.size())
				next = new Integer(0);

			ret[lcv++] =  entriesA[next.intValue()].getEntryReference();
			next = new Integer(next.intValue() + 1);
		}
	    
	    resource.setProperty(_NEXT_PROPERTY_NAME, next);
	    resource.commit();
	    
	    return ret;
	}
	
	@RWXMapping(RWXCategory.INHERITED)
	public CreateFileResponse createFile(CreateFile createFile) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"createFile\" not applicable.");
	}
	
	@RWXMapping(RWXCategory.INHERITED)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		if (addRequest == null)
			throw new RemoteException("\"add\" not applicable without an entry given.");
		
		if (addRequest.getEntry_reference() == null)
			throw new RemoteException("\"add\" not applicable without an entry given.");
		
		return super.add(addRequest);
	}
}
