package edu.virginia.vcgr.genii.container.rns;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;

import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.IteratorBuilder;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;

public class RNSContainerUtilities
{
	static public LookupResponseType translate(
		Iterable<RNSEntryResponseType> entries,
		IteratorBuilder<Object> builder) throws RemoteException
	{
		
		return indexedTranslate(entries, builder, null);
	}

	
	public static LookupResponseType indexedTranslate(
			Iterable<RNSEntryResponseType> entries,
			IteratorBuilder<Object> builder,
			InMemoryIteratorWrapper imiw) throws RemoteException
	{
		
			
		builder.preferredBatchSize(RNSConstants.PREFERRED_BATCH_SIZE);
		builder.addElements(entries);

		IteratorInitializationType iit = builder.create(imiw);
		Collection<RNSEntryResponseType> batch = null;
		IterableElementType []iet = iit.getBatchElement();
		if (iet != null && iet.length > 0)
		{
			batch = new ArrayList<RNSEntryResponseType>(iet.length);
			int lcv = 0;
			for (RNSEntryResponseType t : entries)
			{
				if (lcv >= iet.length)
					break;
				batch.add(t);
				lcv++;
			}
		}
		
		return new LookupResponseType(
			batch == null ? null : batch.toArray(new RNSEntryResponseType[batch.size()]),
			iit.getIteratorEndpoint());
	}
}