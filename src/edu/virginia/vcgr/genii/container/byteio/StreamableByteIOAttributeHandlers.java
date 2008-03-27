package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class StreamableByteIOAttributeHandlers extends ByteIOAttributeHandlers
{
	static public final String STREAMABLE_BYTEIO_NS =
		"http://schemas.ggf.org/byteio/2005/10/streamable-access";
	
	public StreamableByteIOAttributeHandlers(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	private long getPosition() throws ResourceException, ResourceUnknownFaultType
	{
		ISByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISByteIOResource)rKey.dereference();
		Long l = (Long)resource.getProperty(ISByteIOResource.POSITION_PROPERTY);
		return (l != null) ? l.longValue() : 0;
	}
	
	private boolean getSeekable()
	{
		return true;
	}
	
	private boolean getEndOfStream() throws ResourceException, ResourceUnknownFaultType
	{
		ISByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISByteIOResource)rKey.dereference();
		Long l = (Long)resource.getProperty(ISByteIOResource.POSITION_PROPERTY);
		long offset = (l != null) ? l.longValue() : 0;
		File path = resource.getCurrentFile();
		return (offset >= path.length());
	}
	
	private boolean getDestroyOnClose() throws ResourceException, ResourceUnknownFaultType
	{
		ISByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISByteIOResource)rKey.dereference();
		Boolean b = (Boolean)resource.getProperty(ISByteIOResource.DESTROY_ON_CLOSE_PROPERTY);
		if (b == null || !b.booleanValue())
			return false;
		
		return true;
	}
	
	public MessageElement getPositionAttr() 
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetPositionNamespace(), getPosition());
	}
	
	public MessageElement getSeekableAttr() 
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetSeekableNamespace(), getSeekable());
	}
	
	public MessageElement getEndOfStreamAttr() 
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(
				GetEndOfStreamNamespace(), getEndOfStream());
	}
	
	public MessageElement getDestroyOnCloseAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(
			GetDestroyOnCloseNamespace(), getDestroyOnClose());
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		super.registerHandlers();
		
		addHandler(GetPositionNamespace(), "getPositionAttr");
		addHandler(GetSeekableNamespace(), "getSeekableAttr");
		addHandler(GetEndOfStreamNamespace(), "getEndOfStreamAttr");
		addHandler(GetDestroyOnCloseNamespace(), "getDestroyOnCloseAttr");
		
	}
	
	protected QName GetDestroyOnCloseNamespace()
	{
		return ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG;
	}
	
	protected QName GetSizeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Size");
		
	}
	
	protected QName GetPositionNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Position");
	}
	
	protected QName GetReadableNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Readable");
		
	}
	
	protected QName GetWriteableNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Writeable");
		
	}
	
	protected QName GetSeekableNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Seekable");
	}
	
	protected QName GetTransferMechanismNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "TransferMechanism");
		
	}
	
	protected QName GetEndOfStreamNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "EndOfStream");
		
	}
	
	protected QName GetDataResourceNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "DataResource");
		
	}
	
	protected QName GetCreateTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "CreateTime");
		
	}
	
	protected QName GetModificationTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "ModificationTime");
		
	}
	
	protected QName GetAccessTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "AccessTime");
		
	}
}