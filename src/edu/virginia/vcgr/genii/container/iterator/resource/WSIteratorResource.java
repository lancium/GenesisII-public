package edu.virginia.vcgr.genii.container.iterator.resource;

import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface WSIteratorResource extends IResource
{
	static final public String PREFERRED_BATCH_SIZE_PROPERTY = "ws-iterator-preferred-batch-size";

	public long iteratorSize() throws ResourceException;

	public Collection<Pair<Long, MessageElement>> retrieveEntries(int firstElement, int numElements) throws ResourceException;
}