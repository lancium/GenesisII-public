package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOperations;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = ByteIOConstants.BYTEIO_NS,
	name = "ByteIOContentsChangedContents")
public class ByteIOContentsChangedContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(
		namespace = ByteIOConstants.BYTEIO_NS, name = "ResponsibleOperation",
		nillable = false, required = true)
	private ByteIOOperations _operation;
	
	protected ByteIOContentsChangedContents()
	{
	}
	
	public ByteIOContentsChangedContents(ByteIOOperations responsibleOperation)
	{
		_operation = responsibleOperation;
	}
	
	final public ByteIOOperations responsibleOperation()
	{
		return _operation;
	}
}