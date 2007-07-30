package edu.virginia.vcgr.genii.client.comm;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.resource.IResource;

public class ClientConstructionParameters
{
	static public QName TIME_TO_LIVE_PROPERTY_ELEMENT =
		new QName(GenesisIIConstants.GENESISII_NS, "time-to-live");
	
	static public MessageElement createTimeToLiveProperty(long timeToLiveMS)
	{
		MessageElement ret = new MessageElement(TIME_TO_LIVE_PROPERTY_ELEMENT);
		ret.setValue(Long.toString(timeToLiveMS));
		
		return ret;
	}
	
	static public long getTimeToLiveProperty(MessageElement m)
	{
		return Long.parseLong(m.getValue());
	}
	
	static public MessageElement createEndpointIdentifierProperty(java.net.URI epiURI)
	{
		MessageElement ret = new MessageElement(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		ret.setValue(epiURI.toString());
		
		return ret;
	}
	
	static public java.net.URI getEndpointIdentifierProperty(MessageElement m)
		throws java.net.URISyntaxException
	{
		return new java.net.URI((m.getValue()));
	}

}