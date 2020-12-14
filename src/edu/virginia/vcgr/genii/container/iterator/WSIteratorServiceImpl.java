package edu.virginia.vcgr.genii.container.iterator;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.UnsignedLong;
import org.morgan.util.Pair;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.iterator.IteratorConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.iterator.resource.WSIteratorDBResourceProvider;
import edu.virginia.vcgr.genii.container.iterator.resource.WSIteratorResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IterateResponseType;
import edu.virginia.vcgr.genii.iterator.WSIteratorPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@ConstructionParametersType(WSIteratorConstructionParameters.class)
@GeniiServiceConfiguration(resourceProvider = WSIteratorDBResourceProvider.class)
public class WSIteratorServiceImpl extends GenesisIIBase implements WSIteratorPortType
{
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}
	static final public String SERVICE_NAME = "WSIteratorPortType";
	static final private long lifeTime = 1000L * 60 * 5;

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		super.setAttributeHandlers();

		new WSIteratorAttributesHandler(getAttributePackage());
	}

	public WSIteratorServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);

		addImplementedPortType(IteratorConstants.ITERATOR_PORT_TYPE());
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return IteratorConstants.ITERATOR_PORT_TYPE();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateResponseType iterate(IterateRequestType request) throws RemoteException
	{
		WSIteratorResource resource = (WSIteratorResource) ResourceManager.getCurrentResource().dereference();

		extendLifeTime(ResourceManager.getCurrentResource()); // extend the lifetime of this
																// resource between each iteration

		Collection<Pair<Long, MessageElement>> entries =
			resource.retrieveEntries(request.getStartOffset().intValue(), request.getElementCount().intValue());
		IterableElementType[] iterableElements = new IterableElementType[entries.size()];
		int lcv = 0;
		for (Pair<Long, MessageElement> entry : entries) {
			iterableElements[lcv++] = new IterableElementType(new MessageElement[] { entry.second() }, new UnsignedLong(entry.first()));
		}

		return new IterateResponseType(new UnsignedLong(resource.iteratorSize()), iterableElements);
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParameters);
		extendLifeTime(rKey);
	}

	private void extendLifeTime(ResourceKey rKey) throws BaseFaultType, ResourceException
	{
		Calendar future = Calendar.getInstance();
		future.setTimeInMillis(System.currentTimeMillis() + lifeTime);
		setScheduledTerminationTime(future, rKey);
	}
}