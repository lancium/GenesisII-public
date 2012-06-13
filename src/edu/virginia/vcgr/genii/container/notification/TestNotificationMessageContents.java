package edu.virginia.vcgr.genii.container.notification;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = GenesisIIConstants.ENHANCED_NOTIFICATION_BROKER_NS, name = "TestNotification")
public class TestNotificationMessageContents extends NotificationMessageContents {

	private static final long serialVersionUID = 0L;
}
