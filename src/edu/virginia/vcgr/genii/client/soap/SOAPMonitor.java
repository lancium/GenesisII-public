package edu.virginia.vcgr.genii.client.soap;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.utils.xml.XMLStringPrinter;

public class SOAPMonitor extends BasicHandler
{
	static private Log _logger = LogFactory.getLog(SOAPMonitor.class);

	private static final long serialVersionUID = 1L;

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		if (msgContext.getPastPivot()) {
			if (_logger.isDebugEnabled()) {
				Message inMsg = msgContext.getRequestMessage();
				Message outMsg = msgContext.getResponseMessage();

				if (inMsg != null) {
					String formattedXml = XMLStringPrinter.format(inMsg.getSOAPPartAsString());
					_logger.debug("\n\n============== SOAP Request ==============\n" + formattedXml
						+ "==========================================");
				}

				if (outMsg != null) {
					String formattedXml = XMLStringPrinter.format(outMsg.getSOAPPartAsString());
					_logger.debug("\n\n============== SOAP Response ==============\n" + formattedXml
						+ "==========================================");
				}
			}
		}
	}

	public void undo(MessageContext msgContext)
	{
	}
}
