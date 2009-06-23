package edu.virginia.vcgr.genii.container.invoker;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher;
import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncherConsole;
import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.client.comm.GeniiSOAPHeaderConstants;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.container.axis.WSAddressingExtractor;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.version.VersionHelper;

public class SoapHeaderHandler implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(SoapHeaderHandler.class);
	
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
	
	static private QName IS_GENII_ELEMENT_NAME =
		GeniiSOAPHeaderConstants.GENII_ENDPOINT_QNAME;
	static private QName GENII_VERSION_ELEMENT_NAME =
		GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION;
	
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
		Version clientVersion = null;
		boolean isGeniiClient = false;
		
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
				} else if (nodeName.equals(IS_GENII_ELEMENT_NAME))
				{
					Node child = node.getFirstChild();
					if (child != null)
					{
						String value = child.getNodeValue();
						if (value != null && value.equalsIgnoreCase("true"))
							isGeniiClient = true;
					}
				} else if (nodeName.equals(GENII_VERSION_ELEMENT_NAME))
				{
					Node child = node.getFirstChild();
					if (child != null)
					{
						String value = child.getNodeValue();
						if (value != null)
						{
							try
							{
								clientVersion = new Version(value);
							}
							catch (Throwable cause)
							{
								_logger.warn(
									"Can't parse Genii Version soap header.", 
									cause);
								
							}
						}
					}
				}
			}
		}
		
		_logger.debug("Calling Operation with MessageID = " + messageID +
			", ReplyTo = " + replyTo.getAddress().get_value() +
			", Action = " + action);
		
		WorkingContext ctxt = WorkingContext.getCurrentWorkingContext();
		ctxt.setProperty(
			GeniiSOAPHeaderConstants.GENII_ENDPOINT_NAME, isGeniiClient);
		if (clientVersion != null)
			ctxt.setProperty(GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION_NAME,
				clientVersion);
		
		if (isGeniiClient)
		{
			VersionHelper.checkVersion(clientVersion,
				invocationContext.getMethod());
		}
		
		Object obj = invocationContext.proceed();
		msg = msgCtxt.getResponseMessage();
		if (msg == null)
		{
			// It's a one way message
			return obj;
		}

		header = msg.getSOAPHeader();
		
		// Add the genii-endpoint element
		SOAPHeaderElement geniiEndpoint = new SOAPHeaderElement(
			GeniiSOAPHeaderConstants.GENII_ENDPOINT_QNAME, Boolean.TRUE);
		geniiEndpoint.setActor(null);
		geniiEndpoint.setMustUnderstand(false);
		header.addChildElement(geniiEndpoint);
		
		// Add the genii version if appropriate
		ApplicationLauncherConsole console =
			ApplicationLauncher.getConsole();
		if (console != null)
		{
			Version serverVersion = console.currentVersion();
			if (serverVersion != null && 
				!(serverVersion.equals(Version.EMPTY_VERSION)))
			{
				SOAPHeaderElement versionElement = new SOAPHeaderElement(
					GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION,
					serverVersion.toString());
				versionElement.setActor(null);
				versionElement.setMustUnderstand(false);
				header.addChildElement(versionElement);
			}
		}
			
		// Add the relates to
		if (messageID != null)
		{
			SOAPHeaderElement relatesTo = new SOAPHeaderElement(
				new QName(_WS_ADDR_NS, "RelatesTo"), messageID);
			relatesTo.setActor(null);
			relatesTo.setMustUnderstand(false);
			header.addChildElement(relatesTo);
		}
		
		// Add the message id
		SOAPHeaderElement messageid = new SOAPHeaderElement(
			_WS_ADDR_MSG_ID_QNAME, "urn:uuid:" + new GUID());
		messageid.setActor(null);
		messageid.setMustUnderstand(false);
		header.addChildElement(messageid);
		
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
