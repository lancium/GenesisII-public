package edu.virginia.vcgr.genii.container.x509authn;

import java.util.ArrayList;

import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;

import edu.virginia.vcgr.genii.container.rns.IRNSResource;

public interface BaggageAggregatable
{
	ArrayList<RequestSecurityTokenResponseType> aggregateBaggageTokens(IRNSResource resource, RequestSecurityTokenType request)
		throws java.rmi.RemoteException;
}
