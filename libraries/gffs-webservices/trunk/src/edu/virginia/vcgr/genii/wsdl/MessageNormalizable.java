package edu.virginia.vcgr.genii.wsdl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MessageNormalizable extends AbstractXMLComponent implements IXMLComponent
{
	protected QName _message;

	public MessageNormalizable(WsdlSourcePath sourcePath, IXMLComponent parent, Node node) throws WsdlException
	{
		super(sourcePath, parent, node);
	}

	public QName getMessage()
	{
		return _message;
	}

	protected void normalizeMessage() throws WsdlException
	{
		String newMessagePrefix = null;
		String newDocumentPrefix = null;
		String messageNamespace = _message.getNamespaceURI();
		String messageName = _message.getLocalPart();

		String messagePrefix = getMessagePrefix();

		String documentPrefix = _representedNode.lookupPrefix(messageNamespace);
		if (documentPrefix == null) {
			String tmp = _representedNode.lookupNamespaceURI(messagePrefix);
			if (tmp != null) {
				int count = 0;
				while (true) {
					newMessagePrefix = "tns" + count;
					tmp = _representedNode.lookupNamespaceURI(newMessagePrefix);
					if (tmp != null)
						break;
					count++;
				}
			} else {
				newDocumentPrefix = messagePrefix;
			}
		} else {
			if (!documentPrefix.equals(messagePrefix)) {
				newMessagePrefix = documentPrefix;
			}
		}

		Element myElement = (Element) _representedNode;
		if (newMessagePrefix != null) {
			myElement.getAttributeNode(WsdlConstants.MESSAGE_ATTR).setNodeValue(newMessagePrefix + ":" + messageName);
		}
		if (newDocumentPrefix != null) {
			myElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + newDocumentPrefix, messageNamespace);
		}
	}

	protected String getMessagePrefix() throws WsdlException
	{
		String msgString = WsdlUtils.getAttribute(_representedNode.getAttributes(), WsdlConstants.MESSAGE_ATTR, true);
		int index = msgString.indexOf(':');
		return msgString.substring(0, index);
	}
}