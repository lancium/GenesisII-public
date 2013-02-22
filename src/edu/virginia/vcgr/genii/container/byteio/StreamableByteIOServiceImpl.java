package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.ReadNotPermittedFaultType;
import org.ggf.byteio.TransferInformationType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.ggf.byteio.WriteNotPermittedFaultType;
import org.ggf.sbyteio.SeekNotPermittedFaultType;
import org.ggf.sbyteio.SeekRead;
import org.ggf.sbyteio.SeekReadResponse;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.SeekWriteResponse;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.inject.MInject;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsn.base.Subscribe;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOAttributesUpdateNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.SByteIOTopics;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.security.RWXCategory;

@GeniiServiceConfiguration(
	resourceProvider=SByteIOResourceProvider.class)
public class StreamableByteIOServiceImpl extends GenesisIIBase 
	implements StreamableByteIOPortType, ByteIOTopics, SByteIOTopics
{
	/* One Hour Lifetime */
	static private final long SBYTEIO_LIFETIME = 1000L * 60 * 60;
	
	static private Log _logger = LogFactory.getLog(StreamableByteIOServiceImpl.class);
	
	@MInject(lazy = true)
	private ISByteIOResource _resource;
	
	@MInject
	private ResourceLock _resourceLock;
	
	protected Object translateConstructionParameter(MessageElement property)
		throws Exception
	{
		QName name = property.getQName();
		
		if (name.equals(ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER))
		{
			return property.getObjectValue(Subscribe.class);
		} else if (name.equals(ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG))
		{
			return Boolean.parseBoolean(property.getValue());
		} else
			return super.translateConstructionParameter(property);
	}
	
	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new StreamableByteIOAttributeHandlers(getAttributePackage());
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE;
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		ConstructionParameters cParams, HashMap<QName, Object> creationParameters,
		Collection<MessageElement> resolverCreationParams)	
			throws ResourceException, BaseFaultType, RemoteException
	{
		_logger.debug("Creating new StreamableByteIO Resource.");
		super.postCreate(rKey, newEPR, cParams, creationParameters, resolverCreationParams);
		
		ISByteIOResource resource = null;
		
		resource = (ISByteIOResource)rKey.dereference();
		resource.chooseFile(creationParameters);
		
		resource.setProperty(ISByteIOResource.POSITION_PROPERTY, new Long(0));
		
		Boolean destroyOnClose = (Boolean)creationParameters.get(
			ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG);
		if (destroyOnClose == null)
			destroyOnClose = Boolean.FALSE;
		resource.setProperty(ISByteIOResource.DESTROY_ON_CLOSE_PROPERTY, destroyOnClose);
		MetadataType mdt = newEPR.getMetadata();
		if (mdt == null)
			newEPR.setMetadata(mdt = new MetadataType(new MessageElement[] {
				new MessageElement(ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG, destroyOnClose)
			}));
		else
		{
			ArrayList<MessageElement> tmp = new ArrayList<MessageElement>();
			for (MessageElement e : mdt.get_any())
				tmp.add(e);
			tmp.add(new MessageElement(ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG, destroyOnClose));
			mdt.set_any(tmp.toArray(new MessageElement[0]));
		}
		
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		resource.setCreateTime(c);
		resource.setModTime(c);
		resource.setAccessTime(c);
		
		Subscribe s = (Subscribe)creationParameters.get(
			ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER);
		if (s != null)
		{
			WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
			ResourceKey key = (ResourceKey)ctxt.getProperty(WorkingContext.CURRENT_RESOURCE_KEY);
			key.dereference().commit();
			ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY, rKey);
			try
			{
				super.subscribe(s);
			}
			catch (ResourceException re)
			{
				throw re;
			}
			catch (RemoteException re)
			{
				throw new ResourceException(re.getLocalizedMessage(), re);
			}

			finally
			{
				rKey.dereference().commit();
				ctxt.setProperty(WorkingContext.CURRENT_RESOURCE_KEY, key);
				key.dereference().commit();
			}
		}
		
		Calendar future = Calendar.getInstance();
		future.setTimeInMillis(System.currentTimeMillis() + SBYTEIO_LIFETIME);
		setScheduledTerminationTime(future, rKey);
	}
	
	public StreamableByteIOServiceImpl() throws RemoteException
	{
		super("StreamableByteIOPortType");
		
		addImplementedPortType(
			WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}

	
	protected StreamableByteIOServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(
				WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}	
	
	
	
	@RWXMapping(RWXCategory.READ)
	public SeekReadResponse seekRead(SeekRead seekReadRequest)
			throws RemoteException, SeekNotPermittedFaultType, CustomFaultType,
			ReadNotPermittedFaultType, UnsupportedTransferFaultType,
			ResourceUnknownFaultType
	{
		int numBytes = seekReadRequest.getNumBytes().intValue();
		long seekOffset = seekReadRequest.getOffset();
		URI uri = seekReadRequest.getSeekOrigin();
		TransferInformationType transType = seekReadRequest.getTransferInformation();
		
		byte []data = new byte[numBytes];
		File myFile = null;
		RandomAccessFile raf = null;
		
		myFile = _resource.getCurrentFile();
		try
		{
			_resourceLock.lock();
			raf = new RandomAccessFile(myFile, "r");
			
			long offset = ((Long)_resource.getProperty(
				ISByteIOResource.POSITION_PROPERTY)).longValue();
			offset = seek(offset, uri, seekOffset, raf);
				
			int bytesRead = readFully(raf, data, 0, numBytes);
			
			if (bytesRead < numBytes)
			{
				byte []tmp = data;
				data = new byte[bytesRead];
				System.arraycopy(tmp, 0, data, 0, bytesRead);
			}
			
			_resource.setProperty(ISByteIOResource.POSITION_PROPERTY,
				new Long(offset + bytesRead));
			TransferAgent.sendData(data, transType);
		}
		catch(ResourceUnknownFaultType ruft){
			throw FaultManipulator.fillInFault(ruft);
		}
		catch (IOException ioe)
		{
			throw FaultManipulator.fillInFault(
				new CustomFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ioe.toString())
				}, null));
		}
		finally
		{
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		
		return new SeekReadResponse(transType);
	}

	@RWXMapping(RWXCategory.WRITE)
	public SeekWriteResponse seekWrite(SeekWrite seekWriteRequest)
			throws RemoteException, SeekNotPermittedFaultType, CustomFaultType,
			WriteNotPermittedFaultType, UnsupportedTransferFaultType,
			ResourceUnknownFaultType
	{
		long seekOffset = seekWriteRequest.getOffset();
		URI uri = seekWriteRequest.getSeekOrigin();
		TransferInformationType transType = seekWriteRequest.getTransferInformation();
		byte []data = TransferAgent.receiveData(
			transType);
		File myFile = null;
		RandomAccessFile raf = null;
		MessageElement[] byteIOAttrs = null;
		
		myFile = _resource.getCurrentFile();
		try
		{
			_resourceLock.lock();
			raf = new RandomAccessFile(myFile, "rw");
			
			long offset = ((Long)_resource.getProperty(
				ISByteIOResource.POSITION_PROPERTY)).longValue();
			offset = seek(offset, uri, seekOffset, raf);
				
			raf.write(data);
			_resource.setProperty(ISByteIOResource.POSITION_PROPERTY,
				new Long(offset + data.length));
			
			byteIOAttrs = notifyAttributesUpdateAndGetMetadata(myFile, _resource);
		}
		catch (IOException ioe)
		{
			throw FaultManipulator.fillInFault(
				new CustomFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ioe.toString())
				}, null));
		}
		finally
		{
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		
		return new SeekWriteResponse(new TransferInformationType(byteIOAttrs,
                transType.getTransferMechanism()));
	}
	
	static private int readFully(RandomAccessFile raf,
		byte []data, int off, int len) throws IOException
	{
		int r;
		int totalRead = 0;
		
		while ( (r = raf.read(data, off, len)) >= 0)
		{
			off += r;
			len -= r;
			totalRead += r;
			
			if (len <= 0)
				return totalRead;
		}
		
		return totalRead;
	}
	
	static private long seek(long currentPosition,
		URI seekOrigin, long seekOffset, RandomAccessFile raf)
		throws IOException, RemoteException
	{
		long newPosition = currentPosition;
		if (seekOrigin.equals(ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI))
		{
			newPosition = seekOffset;
		} else if (seekOrigin.equals(ByteIOConstants.SEEK_ORIGIN_CURRENT_URI))
		{
			newPosition += seekOffset;
		} else if (seekOrigin.equals(ByteIOConstants.SEEK_ORIGIN_END_URI))
		{
			newPosition = raf.length() + seekOffset;
		} else
		{
			throw new RemoteException("Invalid seek origin given (" +
				seekOrigin.toString() + ").");
		}
		
		raf.seek(newPosition);
		return newPosition;
	}
	
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		
		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic publisherTopic = space.createPublisherTopic(
			SBYTEIO_INSTANCE_DYING);
		publisherTopic.publish(new ResourceTerminationContents(
			Calendar.getInstance()));
	}
	
	/*
	 * This method publish attributes update notification message and also return the updated attributes
	 * as set of MessageElements. We use a single function instead of two for this two operations in order
	 * to reduce the number of database access.
	 * */
	private MessageElement[] notifyAttributesUpdateAndGetMetadata(File currentFile, 
			ISByteIOResource resource) throws ResourceException {

		MessageElement[] attributes = new MessageElement[4];

		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic publisherTopic = space.createPublisherTopic(
				BYTEIO_ATTRIBUTES_UPDATE_TOPIC);
		ByteIOAttributesUpdateNotification message = new ByteIOAttributesUpdateNotification();

		long fileSize = currentFile.length();
		message.setSize(fileSize);
		attributes[0] = new MessageElement(ByteIOConstants.ssize, fileSize);

		Calendar createTime = resource.getCreateTime();
		message.setCreateTime(createTime);
		attributes[1] = new MessageElement(ByteIOConstants.screatTime, createTime);

		Calendar modTime = resource.getModTime();
		message.setModificationTime(modTime);
		attributes[2] = new MessageElement(ByteIOConstants.smodTime, modTime);

		Calendar accessTime = resource.getAccessTime();
		message.setAccessTime(accessTime);
		attributes[3] = new MessageElement(ByteIOConstants.saccessTime, accessTime);

		publisherTopic.publish(message);
		return attributes;
	}
}
