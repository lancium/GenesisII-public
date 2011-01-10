package edu.virginia.vcgr.genii.ui.rns;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.EndpointType;

public interface RNSSelectionFilter
{
	public boolean accept(RNSPath path,
		EndpointReferenceType epr,
		TypeInformation typeInformation,
		EndpointType displayType,
		boolean isLocal);
}