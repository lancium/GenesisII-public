package edu.virginia.vcgr.genii.client.comm.axis;

import java.util.Iterator;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.WSDoAllReceiver;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class DLogHierarchySender extends WSDoAllReceiver {
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(DLogHierarchySender.class);
	static private final String _FLOW_SIDE_KEY = "flow-side";
	static private final String _FLOW_SIDE_REQUEST_VALUE = "request";
	static private final String _FLOW_SIDE_RESPONSE_VALUE = "response";

	private Boolean _isRequest = null;

	private boolean isRequest() throws AxisFault
	{
		synchronized (this)
		{
			if (_isRequest == null)
			{
				AxisFault fault = null;

				String value = (String) getOption(_FLOW_SIDE_KEY);
				if (value != null)
				{
					if (value.equals(_FLOW_SIDE_REQUEST_VALUE))
						_isRequest = Boolean.TRUE;
					else if (value.equals(_FLOW_SIDE_RESPONSE_VALUE))
						_isRequest = Boolean.FALSE;
					else
						fault =
								new AxisFault(_FLOW_SIDE_KEY
										+ " property not recognized.  Expected "
										+ _FLOW_SIDE_REQUEST_VALUE
										+ " or "
										+ _FLOW_SIDE_RESPONSE_VALUE);
				}
				else
				{
					fault =
							new AxisFault("Couldn't find " + _FLOW_SIDE_KEY
									+ " parameter.");
				}

				if (fault != null)
					throw fault;
			}
		}

		return _isRequest.booleanValue();
	}
	
	public DLogHierarchySender() {
		
	}
	
	public void invoke(MessageContext msgContext) throws AxisFault
	{
		if (isRequest()) {
			_logger.debug("Sending a request...");
		}
		else {
			try {
				SOAPMessage msg = msgContext.getMessage();
				@SuppressWarnings("unchecked")
				Iterator<SOAPHeaderElement> rpcids = 
						msg.getSOAPHeader().getChildElements(
						GenesisIIConstants.RPC_ID_QNAME);
				
				if (rpcids.hasNext()) {
					String rpcid = rpcids.next().getValue();
					_logger.debug("Got a response. RPCID is " + rpcid);
				}
				else {
					_logger.debug("Got a response, no RPCID included");
				}
				
			} catch (SOAPException e) {
				_logger.error("Problem retrieving SOAP body from respone message", e);
			}
		}
	}
}
