package edu.virginia.vcgr.genii.container.rfork;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.container.attrs.AttributePackage;

public class StreamableByteIOFactoryAttributeHandlers extends ByteIOAttributeHandlers
{
	static public final String STREAMABLE_BYTEIO_NS = "http://schemas.ggf.org/byteio/2005/10/streamable-access";

	public StreamableByteIOFactoryAttributeHandlers(StreamableByteIOFactoryResourceFork fork, AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(fork, pkg);
	}

	@Override
	protected QName GetCreateTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "CreateTime");
	}

	@Override
	protected QName GetModificationTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "ModificationTime");
	}

	@Override
	protected QName GetAccessTimeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "AccessTime");
	}

	@Override
	protected QName GetReadableNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Readable");
	}

	@Override
	protected QName GetSizeNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Size");
	}

	@Override
	protected QName GetTransferMechanismNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "TransferMechanism");
	}

	@Override
	protected QName GetWriteableNamespace()
	{
		return new QName(STREAMABLE_BYTEIO_NS, "Writeable");
	}
}