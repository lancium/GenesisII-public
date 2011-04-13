package edu.virginia.vcgr.genii.container.iterator;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.iterator.WSIteratorRP;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.iterator.resource.WSIteratorResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

final class WSIteratorAttributesHandler extends AbstractAttributeHandler
{
	private long getElementCount() throws ResourceUnknownFaultType, ResourceException
	{
		WSIteratorResource resource = 
			(WSIteratorResource)ResourceManager.getCurrentResource();
		return resource.iteratorSize();
	}
	
	private long getPreferredBatchSize() throws ResourceException, ResourceUnknownFaultType
	{
		WSIteratorResource resource =
			(WSIteratorResource)ResourceManager.getCurrentResource();
		return (Long)resource.getProperty(WSIteratorResource.PREFERRED_BATCH_SIZE_PROPERTY);
	}
	
	WSIteratorAttributesHandler(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}

	@Override
	final protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(WSIteratorRP.ELEMENT_COUNT_QNAME, "getElementCountAttr");
		addHandler(WSIteratorRP.PREFERRED_BATCH_SIZE_QNAME, 
			"getPreferredBatchSizeAttr");
	}
	
	final public MessageElement getElementCountAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(WSIteratorRP.ELEMENT_COUNT_QNAME,
			getElementCount());
	}
	
	final public MessageElement getPreferredBatchSizeAttr() throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(WSIteratorRP.PREFERRED_BATCH_SIZE_QNAME,
			getPreferredBatchSize());
	}
}
