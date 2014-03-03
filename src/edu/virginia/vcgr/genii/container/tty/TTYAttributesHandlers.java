package edu.virginia.vcgr.genii.container.tty;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;

public class TTYAttributesHandlers extends AbstractAttributeHandler
{
	static private final String NAMESPACE = ByteIOConstants.STREAMABLE_BYTEIO_NS;

	static final private QName READABLE_QNAME = new QName(NAMESPACE, "Readable");
	static final private QName WRITEABLE_QNAME = new QName(NAMESPACE, "Writeable");
	static final private QName SEEKABLE_QNAME = new QName(NAMESPACE, "Seekable");
	static final private QName END_OF_STREAM_QNAME = new QName(NAMESPACE, "EndOfStream");
	static final private QName TRANSFER_MECH_QNAME = new QName(NAMESPACE, "TransferMechanism");
	static final private QName DESTROY_ON_CLOSE_QNAME = new QName(NAMESPACE, "DestroyOnClose");

	public TTYAttributesHandlers(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(READABLE_QNAME, "getReadableAttr");
		addHandler(WRITEABLE_QNAME, "getWriteableAttr");
		addHandler(SEEKABLE_QNAME, "getSeekableAttr");
		addHandler(END_OF_STREAM_QNAME, "getEndOfStreamAttr");
		addHandler(TRANSFER_MECH_QNAME, "getTransferMechsAttr");
		addHandler(DESTROY_ON_CLOSE_QNAME, "getDestroyOnCloseAttr");
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
}