package edu.virginia.vcgr.genii.client.soap;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SOAPMonitor extends BasicHandler
{
	static private Log _logger = LogFactory.getLog(SOAPMonitor.class);
	
	private static final long serialVersionUID = 1L;

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		if (msgContext.getPastPivot())
		{
			Message inMsg = msgContext.getRequestMessage();
			Message outMsg = msgContext.getResponseMessage();

			if (inMsg != null)
			{
				_logger.debug(":::SOAP Request:::\n\n" +
					inMsg.getSOAPPartAsString());
			}

			if (outMsg != null)
			{
				_logger.debug(":::SOAP Response:::\n\n" + 
					outMsg.getSOAPPartAsString());
			}
		}
	}

	public void undo(MessageContext msgContext)
	{
	}
}