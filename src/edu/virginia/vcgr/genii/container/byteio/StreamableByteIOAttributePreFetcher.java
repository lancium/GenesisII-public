package edu.virginia.vcgr.genii.container.byteio;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.container.resource.IResource;

public abstract class StreamableByteIOAttributePreFetcher<Type extends IResource>
	extends DefaultByteIOAttributePreFetcher<Type>
{
	static final private QName XFER_MECHS_ATTR_NAME = new QName(
		ByteIOConstants.STREAMABLE_BYTEIO_NS, "TransferMechanism");
	static final private QName SIZE_ATTR_NAME = new QName(
		ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.SIZE_ATTR_NAME);
	static final private QName ACCESS_TIME_ATTR_NAME = new QName(
		ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.ACCESSTIME_ATTR_NAME);
	static final private QName MOD_TIME_ATTR_NAME = new QName(
		ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.MODTIME_ATTR_NAME);
	static final private QName CREATE_TIME_ATTR_NAME = new QName(	
		ByteIOConstants.STREAMABLE_BYTEIO_NS, ByteIOConstants.CREATTIME_ATTR_NAME);

	protected StreamableByteIOAttributePreFetcher(Type resource)
	{
		super(resource);
	}
	
	@Override
	protected QName getAccessTimeAttributeName()
	{
		return ACCESS_TIME_ATTR_NAME;
	}
	
	@Override
	protected QName getCreateTimeAttributeName()
	{
		return CREATE_TIME_ATTR_NAME;
	}
	
	@Override
	protected QName getModificationTimeAttributeName()
	{
		return MOD_TIME_ATTR_NAME;
	}

	@Override
	protected QName getSizeAttributeName()
	{
		return SIZE_ATTR_NAME;
	}

	@Override
	protected QName getTransferMechanismAttributeName()
	{
		return XFER_MECHS_ATTR_NAME;
	}
}