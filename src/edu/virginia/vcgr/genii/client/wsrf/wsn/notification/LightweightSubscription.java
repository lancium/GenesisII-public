package edu.virginia.vcgr.genii.client.wsrf.wsn.notification;

import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationRegistration;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;

public interface LightweightSubscription extends Subscription
{
	public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
		NotificationHandler<ContentsType> handler);
}