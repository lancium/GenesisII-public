package edu.virginia.vcgr.genii.container.axis;

import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.WSDoAllReceiver;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;
import edu.virginia.vcgr.genii.client.logging.LoggingContext;

public class DLogContextCreator extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(DLogContextCreator.class);
	static private final String _FLOW_SIDE_KEY = "flow-side";
	static private final String _FLOW_SIDE_REQUEST_VALUE = "request";
	static private final String _FLOW_SIDE_RESPONSE_VALUE = "response";

	private Boolean _isRequest = null;

	private boolean isRequest() throws AxisFault
	{
		synchronized (this) {
			if (_isRequest == null) {
				AxisFault fault = null;

				String value = (String) getOption(_FLOW_SIDE_KEY);
				if (value != null) {
					if (value.equals(_FLOW_SIDE_REQUEST_VALUE))
						_isRequest = Boolean.TRUE;
					else if (value.equals(_FLOW_SIDE_RESPONSE_VALUE))
						_isRequest = Boolean.FALSE;
					else
						fault =
							new AxisFault(_FLOW_SIDE_KEY + " property not recognized.  Expected " + _FLOW_SIDE_REQUEST_VALUE
								+ " or " + _FLOW_SIDE_RESPONSE_VALUE);
				} else {
					fault = new AxisFault("Couldn't find " + _FLOW_SIDE_KEY + " parameter.");
				}

				if (fault != null)
					throw fault;
			}
		}

		return _isRequest.booleanValue();
	}

	public DLogContextCreator()
	{

	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		if (isRequest()) {
			// Setup the context for the RPC
			LoggingContext.adoptNewContext();
			String rpcid = DLogUtils.getRPCID();

			_logger.debug("Received a request. RPCID is " + rpcid);
		} else {
			try {
				String rpcid = DLogUtils.getRPCID();

				_logger.debug("Sending a response for " + rpcid);

				SOAPHeaderElement rpcidElement = new SOAPHeaderElement(GenesisIIConstants.RPC_ID_QNAME, rpcid);

				msgContext.getMessage().getSOAPHeader().addChildElement(rpcidElement);
			} catch (SOAPException e) {
				e.printStackTrace();
			} finally {
				// Discard the context for the RPC
				LoggingContext.releaseCurrentLoggingContext();
			}
		}
	}
}
