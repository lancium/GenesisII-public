package edu.virginia.vcgr.genii.client.comm;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.resource.IResource;

public class ClientConstructionParameters
{
	static public QName TIME_TO_LIVE_PROPERTY_ELEMENT =
		new QName(GenesisIIConstants.GENESISII_NS, "time-to-live");
	
	static public QName HUMAN_NAME_PROPERTY_ELEMENT =
		new QName(GenesisIIConstants.GENESISII_NS, "human-name");
	
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

	static public MessageElement createHumanNameProperty(String humanName)
	{
		return new MessageElement(HUMAN_NAME_PROPERTY_ELEMENT, humanName);
	}
	
	static public String getHumanNameProperty(MessageElement me)
	{
		return me.getValue();
	}
}