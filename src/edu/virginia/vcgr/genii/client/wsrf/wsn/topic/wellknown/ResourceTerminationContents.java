package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = WSRFConstants.WSRF_RL_NS, name = "TerminationNotification")
public class ResourceTerminationContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = WSRFConstants.WSRF_RL_NS, name = "TerminationTime", nillable = true, required = true)
	private Calendar _terminationTime;

	@XmlElement(namespace = WSRFConstants.WSRF_RL_NS, name = "TerminationReason", nillable = false, required = false)
	private ResourceTerminationReason _terminationReason = null;

	protected ResourceTerminationContents()
	{
	}

	public ResourceTerminationContents(Calendar terminationTime)
	{
		_terminationTime = terminationTime;
	}

	final public Calendar terminationTime()
	{
		return _terminationTime;
	}

	final public ResourceTerminationReason terminationReason()
	{
		return _terminationReason;
	}
}