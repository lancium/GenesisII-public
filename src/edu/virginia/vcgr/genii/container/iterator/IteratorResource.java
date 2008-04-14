package edu.virginia.vcgr.genii.container.iterator;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.iterator.IteratorConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;

public interface IteratorResource extends IResource
{
	static public final QName ITERATOR_CONSTRUCTION_PARAM_ID =
		new QName(IteratorConstants.ITERATOR_NS, "iterator-id");
	
	public void setIteratorID(String id) throws ResourceException;
	
	public long size() throws ResourceException;
	
	public IteratorMemberType[] get(long startElement, int maxLength)
		throws ResourceException;
}