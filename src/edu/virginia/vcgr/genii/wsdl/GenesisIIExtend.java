package edu.virginia.vcgr.genii.wsdl;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

public class GenesisIIExtend extends AbstractXMLComponent implements IXMLComponent
{
	private QName _extendedPortTypeName;
	private WsdlPortType _extendedPortType = null;

	public GenesisIIExtend(WsdlSourcePath sourcePath, IXMLComponent parent, Node wsdlExtendNode) throws WsdlException
	{
		super(sourcePath, parent, wsdlExtendNode);

		_extendedPortTypeName = WsdlUtils.getQNameFromString(wsdlExtendNode,
			WsdlUtils.getAttribute(wsdlExtendNode.getAttributes(), WsdlConstants.PORT_TYPE_ATTR, true));
	}

	public QName getExtendedPortTypeName()
	{
		return _extendedPortTypeName;
	}

	public WsdlPortType getExtendedPortType() throws WsdlException
	{
		if (_extendedPortType == null) {
			_extendedPortType = findExtendedPortType();
		}

		return _extendedPortType;
	}

	private WsdlPortType findExtendedPortType() throws WsdlException
	{
		WsdlPortType parentPortType = (WsdlPortType) getParent();
		WsdlDocument doc = (WsdlDocument) parentPortType.getParent();
		return doc.findPortType(_extendedPortTypeName);
	}
}