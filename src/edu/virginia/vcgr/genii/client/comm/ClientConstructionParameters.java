package edu.virginia.vcgr.genii.client.comm;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.container.resource.IResource;

public class ClientConstructionParameters
{
	static public MessageElement createEndpointIdentifierProperty(org.apache.axis.types.URI epiURI)
	{
		MessageElement ret = new MessageElement(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		ret.setValue(epiURI.toString());
		
		return ret;
	}
	
	static public org.apache.axis.types.URI getEndpointIdentifierProperty(MessageElement m)
		throws org.apache.axis.types.URI.MalformedURIException
	{
		return new org.apache.axis.types.URI((m.getValue()));
	}
}