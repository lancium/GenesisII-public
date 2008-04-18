package edu.virginia.vcgr.genii.container.ticker;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ogf.ogsa.ticker.CreateTicker;
import org.ogf.ogsa.ticker.CreateTickerResponse;
import org.ogf.ogsa.ticker.TickerFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class TickerServiceImpl
	extends GenesisIIBase implements TickerFactory, TickerConstants
{
	public TickerServiceImpl()
		throws RemoteException
	{
		super("TickerFactory");
		
		addImplementedPortType(new QName(
			TICKER_FACTORY_NS, TICKER_FACTORY_PORT_NAME));
		addImplementedPortType(new QName(
			TICKER_NS, TICKER_PORT_NAME));
	}

	public QName getFinalWSResourceInterface()
	{
		return new QName(TICKER_NS, TICKER_PORT_NAME);
	}
	
	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new TickerAttributesHandler(getAttributePackage());
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters,
			Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, constructionParameters, resolverCreationParams);
		
		IResource resource = key.dereference();
		resource.setProperty(TICKER_CREATION_PROPERTY,
			new Long(System.currentTimeMillis()));
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateTickerResponse createTicker(CreateTicker createTicker)
			throws RemoteException
	{
		return new CreateTickerResponse(
			vcgrCreate(new VcgrCreate(null)).getEndpoint());
	}

	@Override
	public OpenStreamResponse openStream(Object openStreamRequest)
			throws RemoteException, ResourceUnknownFaultType,
			ResourceCreationFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}
}
