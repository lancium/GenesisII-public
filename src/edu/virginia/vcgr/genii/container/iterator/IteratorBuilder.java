package edu.virginia.vcgr.genii.container.iterator;

import java.rmi.RemoteException;
import java.util.Iterator;

import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;

public interface IteratorBuilder<SourceType>
{
	static final public int DEFAULT_PREFERRED_BATCH_SIZE = 20;
	
	public int preferredBatchSize();
	public void preferredBatchSize(int preferredBatchSize);
	
	public void addElements(Iterable<?> iterable);
	public void addElements(Iterator<?> iterator);
	
	public IteratorInitializationType create() throws RemoteException;
	public IteratorInitializationType create(InMemoryIteratorWrapper imiw) throws RemoteException;
}