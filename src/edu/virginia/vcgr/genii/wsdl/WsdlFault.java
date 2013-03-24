package edu.virginia.vcgr.genii.wsdl;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WsdlFault extends MessageNormalizable
{
	public String _name;

	public WsdlFault(WsdlSourcePath sourcePath, IXMLComponent parent, Node wsdlFaultNode) throws WsdlException
	{
		super(sourcePath, parent, wsdlFaultNode);

		NamedNodeMap attrs = wsdlFaultNode.getAttributes();
		String msgStr = WsdlUtils.getAttribute(attrs, WsdlConstants.MESSAGE_ATTR, true);
		_message = WsdlUtils.getQNameFromString(wsdlFaultNode, msgStr);

		_name = WsdlUtils.getAttribute(attrs, WsdlConstants.NAME_ATTR, true);

		normalizeMessage();
	}

	public String getFaultName()
	{
		return _name;
	}
}