package edu.virginia.vcgr.genii.wsdl;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WsdlInputOutput extends MessageNormalizable
{
	private String _soapAction = null;
	
	public WsdlInputOutput(WsdlSourcePath sourcePath, IXMLComponent parent, 
		Node wsdlIONode) throws WsdlException
	{
		super(sourcePath, parent, wsdlIONode);
		
		NamedNodeMap attrs = wsdlIONode.getAttributes();
		String msgStr = WsdlUtils.getAttribute(attrs, 
			WsdlConstants.MESSAGE_ATTR, true);
		_message = WsdlUtils.getQNameFromString(wsdlIONode, msgStr);
		Node actionNode = attrs.getNamedItemNS(
			WsdlConstants.ACTION_QNAME.getNamespaceURI(),
			WsdlConstants.ACTION_QNAME.getLocalPart());
		if (actionNode != null)
			_soapAction = actionNode.getTextContent();
		
		normalizeMessage();
	}
	
	public String getSoapAction()
	{
		return _soapAction;
	}
}