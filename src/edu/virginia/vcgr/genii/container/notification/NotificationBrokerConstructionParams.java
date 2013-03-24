package edu.virginia.vcgr.genii.container.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.naming.jaxb.EPRAdapter;

@XmlAccessorType(XmlAccessType.NONE)
public class NotificationBrokerConstructionParams extends ConstructionParameters
{

	private static final long serialVersionUID = 0L;

	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS, name = "notification-broker-mode", nillable = false, required = false)
	private Boolean mode;

	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS, name = "broker-notification-forwarding-port", nillable = false, required = false)
	@XmlJavaTypeAdapter(EPRAdapter.class)
	private EndpointReferenceType forwardingPort;

	@XmlAttribute(namespace = GenesisIIConstants.GENESISII_NS, name = "scheduled-termination-time", required = true)
	private long scheduledTerminationTime;

	public NotificationBrokerConstructionParams()
	{
	}

	public NotificationBrokerConstructionParams(Boolean mode, EndpointReferenceType forwardingPort,
		long scheduledTerminationTime)
	{
		this.mode = mode;
		this.forwardingPort = forwardingPort;
		this.scheduledTerminationTime = scheduledTerminationTime;
	}

	@XmlTransient
	public Boolean getMode()
	{
		return mode;
	}

	public void setMode(Boolean mode)
	{
		this.mode = mode;
	}

	@XmlTransient
	public EndpointReferenceType getForwardingPort()
	{
		return forwardingPort;
	}

	public void setForwardingPort(EndpointReferenceType forwardingPort)
	{
		this.forwardingPort = forwardingPort;
	}

	@XmlTransient
	public long getScheduledTerminationTime()
	{
		return scheduledTerminationTime;
	}

	public void setScheduledTerminationTime(long scheduledTerminationTime)
	{
		this.scheduledTerminationTime = scheduledTerminationTime;
	}
}
