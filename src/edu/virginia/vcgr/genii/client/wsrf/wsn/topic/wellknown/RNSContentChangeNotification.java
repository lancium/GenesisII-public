package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.container.rns.RNSOperation;

@XmlRootElement(namespace = RNSConstants.GENII_RNS_NS, name = "RNSContentChangeNotification")
public class RNSContentChangeNotification extends NotificationMessageContents
{

	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(RNSContentChangeNotification.class);

	@XmlElement(namespace = RNSConstants.GENII_RNS_NS, name = "Operation", nillable = false, required = true)
	private RNSOperation operation;

	@XmlElement(namespace = RNSConstants.GENII_RNS_NS, name = "RNSEntry", nillable = false, required = false)
	private byte[] entryEPR;

	@XmlElement(namespace = RNSConstants.GENII_RNS_NS, name = "elementCount", nillable = false, required = true)
	private int elementCount;

	public RNSContentChangeNotification()
	{
	}

	public RNSContentChangeNotification(RNSOperation operation, EndpointReferenceType entryEPR, int elementCount,
		MessageElement[] entryAttributes)
	{

		this.operation = operation;
		this.elementCount = elementCount;
		setAdditionalAttributes(entryAttributes);
		if (entryEPR == null) {
			this.entryEPR = null;
		} else {
			try {
				this.entryEPR = EPRUtils.toBytes(entryEPR);
			} catch (ResourceException e) {
				_logger.info("could not convert EPR into byte[]", e);
			}
		}
	}

	@XmlTransient
	public RNSOperation getOperation()
	{
		return operation;
	}

	public void setOperation(RNSOperation operation)
	{
		this.operation = operation;
	}

	@XmlTransient
	public int getElementCount()
	{
		return elementCount;
	}

	public void setElementCount(int elementCount)
	{
		this.elementCount = elementCount;
	}

	@XmlTransient
	public byte[] getEntryEPR()
	{
		return entryEPR;
	}

	public void setEntryEPR(byte[] entryEPR)
	{
		this.entryEPR = entryEPR;
	}

	public EndpointReferenceType getEntry()
	{
		if (entryEPR == null)
			return null;
		try {
			return EPRUtils.fromBytes(entryEPR);
		} catch (ResourceException e) {
			_logger.info("Failed to retrieve EPR from byte[] representation", e);
		}
		return null;
	}
}
