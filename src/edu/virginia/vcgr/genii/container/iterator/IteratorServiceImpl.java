package edu.virginia.vcgr.genii.container.iterator;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.iterator.IteratorConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.iterator.IterateRequestType;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

public class IteratorServiceImpl extends GenesisIIBase implements
		IteratorPortType
{
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters, 
			Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, constructionParameters, resolverCreationParameters);
		
		MessageElement elem = (MessageElement)constructionParameters.get(
			IteratorResource.ITERATOR_CONSTRUCTION_PARAM_ID);
		if (elem == null)
			throw new ResourceException(
				"Unable to construct an iterator without an iterator id.");
		String id = elem.getValue();
		((IteratorResource)key.dereference()).setIteratorID(id);
	}

	public IteratorServiceImpl()
		throws RemoteException
	{
		super("IteratorPortType");
		
		addImplementedPortType(IteratorConstants.ITERATOR_PORT_TYPE_QNAME);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
	}
	
	@Override
	public QName getFinalWSResourceInterface()
	{
		return IteratorConstants.ITERATOR_PORT_TYPE_QNAME;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		throw new RemoteException("Add not supported on iterators.");
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		throw new RemoteException("createFile not supported on iterators.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IteratorMemberType[] iterate(IterateRequestType iterateRequest)
			throws RemoteException
	{
		IteratorResource resource = 
			(IteratorResource)ResourceManager.getCurrentResource().dereference();
		return resource.get(
			iterateRequest.getStartElement().longValue(), 
			iterateRequest.getMaxLength().intValue());
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException("Move not supported on iterators.");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException,
			ResourceUnknownFaultType
	{
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query arg0) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException("Query not supported on iterators.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSDirectoryNotEmptyFaultType
	{
		throw new RemoteException("Remove not supported on iterators.");
	}
}