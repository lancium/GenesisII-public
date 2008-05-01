package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.common.notification.GeniiSubscriptionPortType;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;

public class GeniiSubscriptionServiceImpl extends GenesisIIBase implements
		GeniiSubscriptionPortType
{
	public GeniiSubscriptionServiceImpl() throws RemoteException
	{
		super("GeniiSubscriptionPortType");
		
		addImplementedPortType(WellKnownPortTypes.GENII_SUBSCRIPTION_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_SUBSCRIPTION_PORT_TYPE;
	}
	
	protected Object translateConstructionParameter(MessageElement parameter)
		throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(ISubscriptionResource.SOURCE_KEY_CONSTRUCTION_PARAMETER))
			return parameter.getValue();
		else if (messageName.equals(
			ISubscriptionResource.TARGET_ENDPOINT_CONSTRUCTION_PARAMTER))
			return parameter.getObjectValue(EndpointReferenceType.class);
		else if (messageName.equals(
			ISubscriptionResource.TOPIC_CONSTRUCTION_PARAMETER))
			return parameter.getValue();
		else if (messageName.equals(
			ISubscriptionResource.USER_DATA_CONSTRUCTION_PARAMETER))
			return parameter.getObjectValue(UserDataType.class);
		else
			return super.translateConstructionParameter(parameter);
	}
}