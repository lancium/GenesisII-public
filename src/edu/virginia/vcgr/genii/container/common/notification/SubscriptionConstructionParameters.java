package edu.virginia.vcgr.genii.container.common.notification;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

public class SubscriptionConstructionParameters
{
	static public void insertSubscriptionParameters(
		HashMap<QName, MessageElement> parameters,
		String sourceKey,
		EndpointReferenceType target, String topic, Long timeToLive,
		UserDataType userData)
	{
		parameters.put(ISubscriptionResource.SOURCE_KEY_CONSTRUCTION_PARAMETER,
			new MessageElement(
				ISubscriptionResource.SOURCE_KEY_CONSTRUCTION_PARAMETER,
				sourceKey));
		parameters.put(ISubscriptionResource.TARGET_ENDPOINT_CONSTRUCTION_PARAMTER,
			new MessageElement(
				ISubscriptionResource.TARGET_ENDPOINT_CONSTRUCTION_PARAMTER,
				target));
		parameters.put(ISubscriptionResource.TOPIC_CONSTRUCTION_PARAMETER,
			new MessageElement(
				ISubscriptionResource.TOPIC_CONSTRUCTION_PARAMETER,
				topic));
		if (timeToLive != null)
		{
			MessageElement elem = 
				ClientConstructionParameters.createTimeToLiveProperty(
					timeToLive.longValue());
			parameters.put(elem.getQName(), elem);
		}
		if (userData != null)
		{
			MessageElement elem =
				new MessageElement(
					ISubscriptionResource.USER_DATA_CONSTRUCTION_PARAMETER,
					userData);
			parameters.put(elem.getQName(), elem);
		}
	}
}