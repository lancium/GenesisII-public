package edu.virginia.vcgr.genii.client.rfork;

import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;

import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ResourceForkUtils
{
	static public EndpointReferenceType stripResourceForkInformation(EndpointReferenceType originalEPR)
		throws ResourceException
	{
		MetadataType mt = originalEPR.getMetadata();
		if (mt != null)
			mt = new MetadataType(mt.get_any());

		return new EndpointReferenceType(new AttributedURIType(originalEPR.getAddress().get_value()), new AddressingParameters(
			originalEPR.getReferenceParameters()).toReferenceParameters(), mt, originalEPR.get_any());
	}
}