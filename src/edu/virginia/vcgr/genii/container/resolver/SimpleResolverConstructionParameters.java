package edu.virginia.vcgr.genii.container.resolver;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

public class SimpleResolverConstructionParameters
{
	static public void insertSimpleResolverParameters(
		HashMap<QName, MessageElement> parameters,
		EndpointReferenceType targetEPR,
		String factoryId)
	{
		parameters.put(SimpleResolverServiceImpl.SIMPLE_RESOLVER_TARGET_CONSTRUCTION_PARAMETER,
			new MessageElement(
				SimpleResolverServiceImpl.SIMPLE_RESOLVER_TARGET_CONSTRUCTION_PARAMETER,
				targetEPR));
		parameters.put(SimpleResolverServiceImpl.SIMPLE_RESOLVER_FACTORY_EPI_CONSTRUCTION_PARAMETER,
				new MessageElement(
					SimpleResolverServiceImpl.SIMPLE_RESOLVER_FACTORY_EPI_CONSTRUCTION_PARAMETER,
					factoryId));
	}
}