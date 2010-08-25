package edu.virginia.vcgr.genii.client.naming.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class EPRAdapter 
	extends XmlAdapter<byte[], EndpointReferenceType>
{
	@Override
	public byte[] marshal(EndpointReferenceType arg0) throws Exception
	{
		return EPRUtils.toBytes(arg0);
	}

	@Override
	public EndpointReferenceType unmarshal(byte[] arg0) throws Exception
	{
		return EPRUtils.fromBytes(arg0);
	}
}