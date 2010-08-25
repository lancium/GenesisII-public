package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = BESConstants.GENII_BES_NS,
	name = "BESActivityStateChangedContents")
public class BESActivityStateChangedContents
	extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = BESConstants.GENII_BES_NS,
		name = "ActivityState", nillable = false, required = true)
	private ActivityState _activityState = null;
	
	protected BESActivityStateChangedContents()
	{	
	}
	
	public BESActivityStateChangedContents(
		ActivityState state)
	{
		_activityState = state;
	}
	
	final public ActivityState activityState()
	{
		return _activityState;
	}
}