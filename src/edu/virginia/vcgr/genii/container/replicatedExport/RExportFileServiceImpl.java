package edu.virginia.vcgr.genii.container.replicatedExport;

import java.rmi.RemoteException;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.ReadNotPermittedFaultType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.ggf.rbyteio.Write;
import org.ggf.rbyteio.WriteResponse;
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

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributeHandlers;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.replicatedExport.RExportFilePortType;

public class RExportFileServiceImpl extends RandomByteIOServiceImpl 
	implements RExportFilePortType
{
	static private Log _logger = LogFactory.getLog(RExportFileServiceImpl.class);
	
	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new RandomByteIOAttributeHandlers(getAttributePackage());
	}
	
	public RExportFileServiceImpl() throws RemoteException
	{
		this("RExportFilePortType");
		
		addImplementedPortType(
				WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
		
	}
	
	protected RExportFileServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		addImplementedPortType(
				WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
		addImplementedPortType(
				WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	public WriteResponse write(Write write)
		throws RemoteException, CustomFaultType, 
			ReadNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		WriteResponse writeResp = super.write(write);
		
		return writeResp; 
	}
	
	private class LegacyResourceTerminatedNotificationHandler
		extends AbstractNotificationHandler<ResourceTerminationContents>
	{
		private LegacyResourceTerminatedNotificationHandler()
		{
			super(ResourceTerminationContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ResourceTerminationContents contents) throws Exception
		{
			RExportSubscriptionUserData notifyData = 
				contents.additionalUserData(RExportSubscriptionUserData.class);
			
			String exportPath = notifyData.getPrimaryLocalPath();
			
			destroy(new Destroy());
			
			_logger.info("RExport replica " + exportPath + " terminated.");
		}
	}

	@Override
	protected void registerNotificationHandlers(
		NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		multiplexer.registerNotificationHandler(
			GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(),
			new LegacyResourceTerminatedNotificationHandler());
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("add operation not supported in replicated export");
	}

	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("createFile operation not supported in replicated export");
	}

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("list operation not supported in replicated export");
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("Move operation not supported in replicated export");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("Query operation not supported in replicated export");
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSDirectoryNotEmptyFaultType, RNSFaultType
	{
		throw new RemoteException("remove operation not supported in replicated export");
	}
	
	protected Object translateConstructionParameter(MessageElement parameter)
	throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(IRExportResource.LOCALPATH_CONSTRUCTION_PARAM))
			return parameter.getValue();
		else
			return super.translateConstructionParameter(parameter);
	}
	
}