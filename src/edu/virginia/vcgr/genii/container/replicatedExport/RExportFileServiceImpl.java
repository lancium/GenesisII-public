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
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.container.byteio.RandomByteIOAttributeHandlers;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.notification.TopicSpace;
import edu.virginia.vcgr.genii.replicatedExport.RExportFilePortType;


public class RExportFileServiceImpl extends RandomByteIOServiceImpl 
	implements RExportFilePortType
{
	static private Log _logger = LogFactory.getLog(RExportFileServiceImpl.class);
	
	protected void setAttributeHandlers() throws NoSuchMethodException
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
	
	protected void registerTopics(TopicSpace topicSpace)
		throws InvalidTopicException
	{
		super.registerTopics(topicSpace);
		topicSpace.registerTopic(WellknownTopics.RANDOM_BYTEIO_OP);
	}
	
	public WriteResponse write(Write write)
		throws RemoteException, CustomFaultType, 
			ReadNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		WriteResponse writeResp = super.write(write);
		
		return writeResp; 
	}
	
	/* NotificationConsumer port type */
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) 
		throws RemoteException, ResourceUnknownFaultType
	{
		try{
			String topic = notify.getTopic().toString();
			
			//destroy replica if notified of resolver termination
			if (topic.equals(WellknownTopics.TERMINATED)){
				RExportSubscriptionUserData notifyData = new RExportSubscriptionUserData(
						notify.getUserData());
				
				String exportPath = notifyData.getPrimaryLocalPath();
				
				destroy(new Destroy());
				
				_logger.info("RExport replica " + exportPath + " terminated.");
			}
		}
		catch (Throwable t){
			_logger.warn(t.getLocalizedMessage(), t);
		}
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