package edu.virginia.vcgr.genii.container.invoker;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.container.axis.WSAddressingExtractor;

public class WSAddressingHandler implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(WSAddressingHandler.class);
	
	static private final String _WS_ADDR_NS = 
		"http://www.w3.org/2005/08/addressing";
	
	static private final String _WS_ADDR_MSG_ID = "MessageID";
	static private final String _WS_ADDR_REPLY_TO = "ReplyTo";
	static private final String _WS_ADDR_ACTION = "Action";
	
	static private QName _WS_ADDR_MSG_ID_QNAME = 
		new QName(_WS_ADDR_NS, _WS_ADDR_MSG_ID);
	static private QName _WS_ADDR_REPLY_TO_QNAME = 
		new QName(_WS_ADDR_NS, _WS_ADDR_REPLY_TO);
	static private QName _WS_ADDR_ACTION_QNAME = 
		new QName(_WS_ADDR_NS, _WS_ADDR_ACTION);
	
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		MessageContext msgCtxt = invocationContext.getMessageContext();
		
		EndpointReferenceType target = (EndpointReferenceType)msgCtxt.getProperty(
			WSAddressingExtractor.AXIS_MESSAGE_CTXT_EPR_PROPERTY);
		
		if (target == null)
		{
			_logger.warn("Couldn't find target EPR in Working Context.");
			return invocationContext.proceed();
		}
		
		String messageID = null;
		EndpointReferenceType replyTo = EPRUtils.makeEPR(
			"http://www.w3.org/2005/08/addressing/anonymous", false);
		String action = null;
		
		Message msg = msgCtxt.getRequestMessage();
		SOAPHeader header = msg.getSOAPHeader();
		NodeList list = header.getChildNodes();
		int length = list.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node node = list.item(lcv);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				QName nodeName = new QName(node.getNamespaceURI(),
					node.getLocalName());
				
				if (nodeName.equals(_WS_ADDR_MSG_ID_QNAME))
				{
					messageID = node.getFirstChild().getNodeValue();
				} else if (nodeName.equals(_WS_ADDR_REPLY_TO_QNAME))
				{
					// We have an EPR, it's hard to parse those but we ONLY need the address part of
					// it anyways
					NodeList list2 = node.getChildNodes();
					int length2 = list2.getLength();
					for (int lcv2 = 0; lcv2 < length2; lcv2++)
					{
						Node node2 = list2.item(lcv2);
						if (node2.getNodeType() == Node.ELEMENT_NODE)
						{
							QName node2Name = new QName(node2.getNamespaceURI(),
								node2.getLocalName());
							
							if (node2Name.equals(new QName(_WS_ADDR_NS, "Address")))
							{
								replyTo = EPRUtils.makeEPR(node2.getFirstChild().getNodeValue(), false);
								break;
							}
						}
					}
				} else if (nodeName.equals(_WS_ADDR_ACTION_QNAME))
				{
					action = node.getFirstChild().getNodeValue();
				}
			}
		}
		
		_logger.debug("Calling Operation with MessageID = " + messageID +
			", ReplyTo = " + replyTo.getAddress().get_value() +
			", Action = " + action);
		
		Object obj = invocationContext.proceed();
		msg = msgCtxt.getResponseMessage();
		if (msg == null)
		{
			// It's a one way message
			return obj;
		}

		header = msg.getSOAPHeader();
		
		// Add the relates to
		if (messageID != null)
		{
			SOAPHeaderElement relatesTo = new SOAPHeaderElement(
				new QName(_WS_ADDR_NS, "RelatesTo"), messageID);
			relatesTo.setActor(null);
			relatesTo.setMustUnderstand(false);
			header.addChildElement(relatesTo);
		}
		
		// Add the To element
		SOAPHeaderElement to = new SOAPHeaderElement(
			new QName(_WS_ADDR_NS, "To"), 
			replyTo.getAddress().get_value().toString());
		to.setActor(null);
		to.setMustUnderstand(false);
		header.addChildElement(to);
		
		// Add the action element
		if (action != null)
		{
			SOAPHeaderElement actionE = new SOAPHeaderElement(
				_WS_ADDR_ACTION_QNAME,
				action + "Response");
			actionE.setActor(null);
			actionE.setMustUnderstand(false);
			header.addChildElement(actionE);
		}
		
		return obj;
	}
}
