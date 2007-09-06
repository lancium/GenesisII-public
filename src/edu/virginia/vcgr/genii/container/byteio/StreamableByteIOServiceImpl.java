package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.Calendar;
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
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.common.notification.Topic;
import edu.virginia.vcgr.genii.container.common.notification.TopicSpace;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class StreamableByteIOServiceImpl extends GenesisIIBase implements
		StreamableByteIOPortType
{
	static private Log _logger = LogFactory.getLog(RandomByteIOServiceImpl.class);
	
	protected Object translateConstructionParameter(MessageElement property)
		throws Exception
	{
		QName name = property.getQName();
		
		if (name.equals(ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER))
		{
			return property.getObjectValue(Subscribe.class);
		} else
			return super.translateConstructionParameter(property);
	}
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new StreamableByteIOAttributeHandlers(getAttributePackage());
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> creationParameters)	
			throws ResourceException, BaseFaultType, RemoteException
	{
		_logger.debug("Creating new StreamableByteIO Resource.");
		
		super.postCreate(rKey, newEPR, creationParameters);
		
		ISByteIOResource resource = null;
		
		resource = (ISByteIOResource)rKey.dereference();
		resource.chooseFile(creationParameters);
		
		resource.setProperty(ISByteIOResource.POSITION_PROPERTY, new Long(0));
		
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
		ISByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "r");
				
				long offset = ((Long)resource.getProperty(
					ISByteIOResource.POSITION_PROPERTY)).longValue();
				offset = seek(offset, uri, seekOffset, raf);
					
				int bytesRead = readFully(raf, data, 0, numBytes);
				
				if (bytesRead < numBytes)
				{
					byte []tmp = data;
					data = new byte[bytesRead];
					System.arraycopy(tmp, 0, data, 0, bytesRead);
				}
				
				resource.setProperty(ISByteIOResource.POSITION_PROPERTY,
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
			}
		}
		
		resource.commit();
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
		ISByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "rw");
				
				long offset = ((Long)resource.getProperty(
					ISByteIOResource.POSITION_PROPERTY)).longValue();
				offset = seek(offset, uri, seekOffset, raf);
					
				raf.write(data);
				resource.setProperty(ISByteIOResource.POSITION_PROPERTY,
					new Long(offset + data.length));
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
			}
		}
		
		resource.commit();
		return new SeekWriteResponse(new TransferInformationType(null,
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
	
	protected void registerTopics(TopicSpace topicSpace) throws InvalidTopicException
	{
		super.registerTopics(topicSpace);
		
		topicSpace.registerTopic(WellknownTopics.SBYTEIO_INSTANCE_DYING);
	}
	
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		
		Topic t = getTopicSpace().getTopic(WellknownTopics.SBYTEIO_INSTANCE_DYING);
		t.notifyAll(null);
	}
}