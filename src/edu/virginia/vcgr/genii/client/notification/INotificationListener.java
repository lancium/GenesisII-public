package edu.virginia.vcgr.genii.client.notification;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

/**
 * This interface must be implemented by all client handlers that wish to handle
 * notifications received by the NoticationServer class.
 * 
 * @author mmm2a
 */
public interface INotificationListener
{
	/**
	 * Handle a received notification.
	 * 
	 * @param subscription The subscription that was handed out when this listener was
	 * registered.
	 * @param source The EPR of the source of this notification.
	 * @param topic The topic that this notification refers to.
	 * @param notificationData Payload information in the notification (dependent on
	 * the type of notification produced).
	 */
	public void notify(ISubscription subscription,
		EndpointReferenceType source, String topic,
		MessageElement []notificationData);
}