package edu.virginia.vcgr.genii.client.wsrf.wsn.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage;
import org.oasis_open.wsn.base.Notify;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;

public class NotificationHelper
{
	static private Log _logger = LogFactory.getLog(NotificationHelper.class);
	
	static public void notify(Notify notification, NotificationMultiplexer multiplexer)
	{
		NotificationMessageHolderType []msgs = notification.getNotificationMessage();
		if (msgs != null)
		{
			for (NotificationMessageHolderType message : msgs)
			{
				notifySingleMessage(message, multiplexer);
			}
		}
	}
	
	static public String notifySingleMessage(
			NotificationMessageHolderType message,
			NotificationMultiplexer multiplexer)
	{
		try
		{
			NotificationMessageHolder holder = new NotificationMessageHolder(null, message);
			EndpointReferenceType producer = holder.publisherReference();
			EndpointReferenceType subscription = holder.subscriptionReference();

			NotificationMessageHolderTypeMessage contents = message.getMessage();
			Element []eContents = null;
			Element eContent = null;
			if (contents != null)
			{
				eContents = contents.get_any();
				if (eContents != null)
				{
					if (eContents.length > 0)
						eContent = eContents[0];
				}
			}
			return multiplexer.notify(holder.topic(), producer, subscription, eContent);
		}
		catch (Throwable cause)
		{
			_logger.warn("Got a notification message that we can't handle.", cause);
			return NotificationConstants.FAIL;
		}	
	}
}