package edu.virginia.vcgr.genii.client.comm.axis;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.ws.axis.security.WSDoAllReceiver;

import edu.virginia.vcgr.genii.client.logging.LoggingContext;

public class DLogContextVerifier extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	public DLogContextVerifier()
	{
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		// Setup the context for the RPC
		if (!LoggingContext.hasCurrentLoggingContext())
			LoggingContext.assumeNewLoggingContext();
	}
}
