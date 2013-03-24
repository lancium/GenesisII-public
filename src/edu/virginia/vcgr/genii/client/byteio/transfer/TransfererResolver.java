package edu.virginia.vcgr.genii.client.byteio.transfer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public class TransfererResolver
{
	static private Log _logger = LogFactory.getLog(TransfererResolver.class);

	/**
	 * Consider the following sequence of events: 1. The client sends a "read" request to the
	 * server. 2. The server fully processes the "read" request. It reads the data from disk into
	 * memory, and it sends a SOAP response with the data in an attachment. 3. The client receives
	 * the SOAP response. 4. The client receives some of the attachment. 5. The connection fails
	 * before the transfer is complete.
	 * 
	 * Was this RPC a success or a failure?
	 * 
	 * To the low-level (i.e. AxisClientInvocationHandler), it was a success. The server finished
	 * processing the request. The client must not re-send the request, in case the actions that the
	 * server took while processing the request were not idempotent.
	 * 
	 * To the high level (i.e. RandomByteIOInputStream), it was a failure. The input stream knows
	 * that the request is idempotent, and that the response is meaningless without its attachment.
	 * 
	 * When a "read" request succeeds at the low-level but fails at the high-level, call this
	 * function. This function checks if the file has a replica which might be able to service the
	 * read request.
	 */
	public static RandomByteIOPortType resolveClientStub(RandomByteIOPortType clientStub) throws ResourceException,
		GenesisIISecurityException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("resolveClientStub: entered");
		EndpointReferenceType primary = ClientUtils.extractEPR(clientStub);
		EndpointReferenceType replica = null;
		try {
			replica = ResolverUtils.resolve(primary);
		} catch (Exception exception) {
			if (_logger.isDebugEnabled())
				_logger.debug("resolveClientStub: resolver failed: " + exception);
		}
		if (replica == null) {
			if (_logger.isDebugEnabled())
				_logger.debug("resolveClientStub: no replica");
			return null;
		}
		if (_logger.isDebugEnabled())
			_logger.debug("resolveClientStub: replica=" + replica.getAddress());
		return ClientUtils.createProxy(RandomByteIOPortType.class, replica);
	}
}
