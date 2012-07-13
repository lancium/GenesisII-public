package edu.virginia.vcgr.genii.container.axis;

import java.util.Iterator;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.context.ClientConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientConfigHandler extends BasicHandler {

	private static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ClientConfigHandler.class);

	@Override
	public void invoke(MessageContext msgContext) throws AxisFault {
		try {
			SOAPHeader header = msgContext.getCurrentMessage().getSOAPHeader();
			Iterator<?> iterator = header.examineAllHeaderElements();
			while (iterator.hasNext()) {
				SOAPHeaderElement element = (SOAPHeaderElement) iterator.next();
				if (element.getNamespaceURI().equals(GenesisIIConstants.GENESISII_NS) 
						&& element.getLocalName().equals(GenesisIIConstants.CLIENT_ID_ATTRIBUTE_NAME)) {
					String clientId = element.getValue();
					ClientConfig.setClientConfig(clientId);
					break;
				} else continue;
			}
		} catch (SOAPException e) {
			_logger.info("exception occurred in invoke", e);
		}
	}
}
