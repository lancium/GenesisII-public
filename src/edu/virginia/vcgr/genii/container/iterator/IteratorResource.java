package edu.virginia.vcgr.genii.container.iterator;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;

public interface IteratorResource extends IResource
{
	public void setIteratorID(String id) throws ResourceException;
	
	public long size() throws ResourceException;
	
	public IteratorMemberType[] get(long startElement, int maxLength)
		throws ResourceException;
}