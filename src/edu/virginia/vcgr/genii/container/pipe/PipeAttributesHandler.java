package edu.virginia.vcgr.genii.container.pipe;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.pipe.PipeConstants;
import edu.virginia.vcgr.genii.client.pipe.PipeConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class PipeAttributesHandler extends AbstractAttributeHandler
{
	static private final String SBYTEIO_NAMESPACE = ByteIOConstants.STREAMABLE_BYTEIO_NS;
	static private final String PIPE_NS = PipeConstants.PIPE_NS;

	static final private QName SIZE_QNAME = new QName(SBYTEIO_NAMESPACE, "Size");
	static final private QName READABLE_QNAME = new QName(SBYTEIO_NAMESPACE, "Readable");
	static final private QName WRITEABLE_QNAME = new QName(SBYTEIO_NAMESPACE, "Writeable");
	static final private QName SEEKABLE_QNAME = new QName(SBYTEIO_NAMESPACE, "Seekable");
	static final private QName END_OF_STREAM_QNAME = new QName(SBYTEIO_NAMESPACE, "EndOfStream");
	static final private QName TRANSFER_MECH_QNAME = new QName(SBYTEIO_NAMESPACE, "TransferMechanism");
	static final private QName DESTROY_ON_CLOSE_QNAME = new QName(SBYTEIO_NAMESPACE, "DestroyOnClose");

	static final private QName PIPE_SIZE_QNAME = new QName(PIPE_NS, "PipeSize");

	public PipeAttributesHandler(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(SIZE_QNAME, "getSizeAttr");
		addHandler(READABLE_QNAME, "getReadableAttr");
		addHandler(WRITEABLE_QNAME, "getWriteableAttr");
		addHandler(SEEKABLE_QNAME, "getSeekableAttr");
		addHandler(END_OF_STREAM_QNAME, "getEndOfStreamAttr");
		addHandler(TRANSFER_MECH_QNAME, "getTransferMechsAttr");
		addHandler(DESTROY_ON_CLOSE_QNAME, "getDestroyOnCloseAttr");

		addHandler(PIPE_SIZE_QNAME, "getPipeSizeAttr");
	}

	public MessageElement getSizeAttr()
	{
		return new MessageElement(SIZE_QNAME, 0L);
	}

	public MessageElement getReadableAttr()
	{
		return new MessageElement(READABLE_QNAME, Boolean.TRUE);
	}

	public MessageElement getWriteableAttr()
	{
		return new MessageElement(WRITEABLE_QNAME, Boolean.TRUE);
	}

	public MessageElement getSeekableAttr()
	{
		return new MessageElement(SEEKABLE_QNAME, Boolean.FALSE);
	}

	public MessageElement getEndOfStreamAttr()
	{
		return new MessageElement(END_OF_STREAM_QNAME, Boolean.FALSE);
	}

	public MessageElement getDestroyOnCloseAttr()
	{
		return new MessageElement(DESTROY_ON_CLOSE_QNAME, Boolean.FALSE);
	}

	public Collection<MessageElement> getTransferMechsAttr()
	{
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();
		QName name = TRANSFER_MECH_QNAME;

		ret.add(new MessageElement(name, ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		ret.add(new MessageElement(name, ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		ret.add(new MessageElement(name, ByteIOConstants.TRANSFER_TYPE_MTOM_URI));

		return ret;
	}

	public MessageElement getPipeSizeAttr() throws ResourceUnknownFaultType, ResourceException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		PipeConstructionParameters consParms = (PipeConstructionParameters) resource
			.constructionParameters(PipeServiceImpl.class);

		return new MessageElement(PIPE_SIZE_QNAME, consParms.pipeSize());
	}
}