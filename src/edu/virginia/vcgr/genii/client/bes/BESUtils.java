package edu.virginia.vcgr.genii.client.bes;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityDocumentType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;

public class BESUtils
{
	static public void addSubscription(ActivityDocumentType adt,
		SubscribeRequest s)
	{
		Collection<MessageElement> anyC;
		
		MessageElement []any = adt.get_any();
		if (any == null)
			anyC = new ArrayList<MessageElement>(1);
		else
		{
			anyC = new ArrayList<MessageElement>(any.length + 1);
			for (MessageElement a : any)
				anyC.add(a);
		}
		
		anyC.add(new MessageElement(
			BESConstants.GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT_QNAME,
			s.asRequestType()));
		adt.set_any(anyC.toArray(new MessageElement[0]));
	}
}