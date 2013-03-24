package edu.virginia.vcgr.genii.wsdl;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WsdlPortType extends AbstractXMLComponent implements IXMLComponent
{
	private String _name;
	private HashMap<QName, GenesisIIExtend> _extensions = new HashMap<QName, GenesisIIExtend>();
	private HashMap<String, WsdlOperation> _operations = new HashMap<String, WsdlOperation>();

	public WsdlPortType(WsdlSourcePath sourcePath, IXMLComponent parent, Node portTypeNode) throws WsdlException
	{
		super(sourcePath, parent, portTypeNode);

		_name = WsdlUtils.getAttribute(portTypeNode.getAttributes(), WsdlConstants.NAME_ATTR, true);

		NodeList children = _representedNode.getChildNodes();
		for (int lcv = 0; lcv < children.getLength(); lcv++) {
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				QName childName = WsdlUtils.getQName(child);

				if (childName.equals(WsdlConstants.EXTEND_QNAME)) {
					GenesisIIExtend extend = new GenesisIIExtend(_sourcePath, this, child);
					_extensions.put(extend.getExtendedPortTypeName(), extend);
				} else if (childName.equals(WsdlConstants.OPERATION_QNAME)) {
					WsdlOperation oper = new WsdlOperation(_sourcePath, this, child);
					_operations.put(oper.getOperationName(), oper);
				}
			}
		}
	}

	public QName getName()
	{
		return new QName(findTargetNamespace(), _name);
	}

	public Collection<WsdlOperation> getOperations() throws WsdlException
	{
		normalize();

		return _operations.values();
	}

	public void normalize() throws WsdlException
	{
		for (GenesisIIExtend extension : _extensions.values()) {
			WsdlPortType extendedPortType = extension.getExtendedPortType();
			for (WsdlOperation oper : extendedPortType.getOperations()) {
				if (_operations.containsKey(oper.getOperationName()))
					continue;

				Node newNode = _representedNode.getOwnerDocument().importNode(oper.getRepresentedNode(), true);
				_representedNode.appendChild(newNode);
				WsdlOperation newOperation;

				try {
					AlternativeNamespaceResolution.setAlternativeResolver(oper.getRepresentedNode().getOwnerDocument());
					newOperation = new WsdlOperation(_sourcePath, this, newNode);
				} finally {
					AlternativeNamespaceResolution.setAlternativeResolver(null);
				}
				_operations.put(newOperation.getOperationName(), newOperation);
			}

			_representedNode.removeChild(extension.getRepresentedNode());
		}

		_extensions.clear();
	}
}