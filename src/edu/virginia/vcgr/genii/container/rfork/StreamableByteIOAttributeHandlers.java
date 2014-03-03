package edu.virginia.vcgr.genii.container.rfork;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;

public class StreamableByteIOAttributeHandlers extends ByteIOAttributeHandlers
{
	static public final String STREAMABLE_BYTEIO_NS = "http://schemas.ggf.org/byteio/2005/10/streamable-access";

	public StreamableByteIOAttributeHandlers(StreamableByteIOResourceFork fork, AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(fork, pkg);
	}

	private long getPosition() throws ResourceException, ResourceUnknownFaultType
	{
		return ((StreamableByteIOResourceFork) _fork).getPosition();
	}

	private boolean getSeekable()
	{
		return ((StreamableByteIOResourceFork) _fork).getSeekable();
	}

	private boolean getEndOfStream() throws ResourceException, ResourceUnknownFaultType
	{
		return ((StreamableByteIOResourceFork) _fork).getEndOfStream();
	}

	private boolean getDestroyOnClose() throws ResourceException, ResourceUnknownFaultType
	{
		return ((StreamableByteIOResourceFork) _fork).getDestroyOnClose();
	}

	public MessageElement getPositionAttr() throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetPositionNamespace(), getPosition());
	}

	public MessageElement getSeekableAttr() throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetSeekableNamespace(), getSeekable());
	}

	public MessageElement getEndOfStreamAttr() throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetEndOfStreamNamespace(), getEndOfStream());
	}

	public MessageElement getDestroyOnCloseAttr() throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(GetDestroyOnCloseNamespace(), getDestroyOnClose());
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