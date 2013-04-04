package edu.virginia.vcgr.genii.container.x509authn;

import java.util.ArrayList;

import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;

public interface BaggageAggregatable
{
	ArrayList<RequestSecurityTokenResponseType> aggregateBaggageTokens(RequestSecurityTokenType request)
		throws java.rmi.RemoteException;
}
