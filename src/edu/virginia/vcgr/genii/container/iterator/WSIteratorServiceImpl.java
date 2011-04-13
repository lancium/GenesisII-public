package edu.virginia.vcgr.genii.container.iterator;

import java.rmi.RemoteException;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.UnsignedLong;
import org.morgan.util.Pair;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.iterator.IteratorConstants;
import edu.virginia.vcgr.genii.client.iterator.WSIteratorConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.iterator.resource.WSIteratorDBResourceProvider;
import edu.virginia.vcgr.genii.container.iterator.resource.WSIteratorResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;

@ConstructionParametersType(WSIteratorConstructionParameters.class)
@GeniiServiceConfiguration(
	resourceProvider=WSIteratorDBResourceProvider.class)
public class WSIteratorServiceImpl extends GenesisIIBase
	implements WSIteratorPortType
{
	static final public String SERVICE_NAME = "WSIteratorPortType";
	
	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new WSIteratorAttributesHandler(getAttributePackage());
	}


	public WSIteratorServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);
		
		addImplementedPortType(IteratorConstants.ITERATOR_PORT_TYPE);
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return IteratorConstants.ITERATOR_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateResponseType iterate(IterateRequestType request)
		throws RemoteException
	{
		WSIteratorResource resource = 
			(WSIteratorResource)ResourceManager.getCurrentResource().dereference();
		Collection<Pair<Long, MessageElement>> entries = resource.retrieveEntries(
			request.getStartOffset().intValue(),
			request.getElementCount().intValue());
		IterableElementType []iterableElements = new IterableElementType[entries.size()];
		int lcv = 0;
		for (Pair<Long, MessageElement> entry : entries)
		{
			iterableElements[lcv++] = new IterableElementType(
				new MessageElement[] { entry.second() }, new UnsignedLong(entry.first()));
		}
		
		return new IterateResponseType(new UnsignedLong(resource.iteratorSize()),
			iterableElements);
	}
}