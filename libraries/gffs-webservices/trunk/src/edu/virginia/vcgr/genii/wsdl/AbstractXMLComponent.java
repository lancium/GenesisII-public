package edu.virginia.vcgr.genii.wsdl;

import org.w3c.dom.Node;

public abstract class AbstractXMLComponent implements IXMLComponent
{
	protected WsdlSourcePath _sourcePath;
	protected IXMLComponent _parent;
	protected Node _representedNode;
	protected String _targetNamespace = null;

	protected AbstractXMLComponent(WsdlSourcePath sourcePath, IXMLComponent parent, Node representedNode) throws WsdlException
	{
		_parent = parent;
		_representedNode = representedNode;
		if (sourcePath == null)
			_sourcePath = new WsdlSourcePath();
		else
			_sourcePath = sourcePath;
	}

	public IXMLComponent getParent()
	{
		return _parent;
	}

	public Node getRepresentedNode()
	{
		return _representedNode;
	}

	public String findTargetNamespace()
	{
		if (_targetNamespace == null) {
			_targetNamespace = WsdlUtils.findHierarchicalAttribute(_representedNode, WsdlConstants.TARGET_NAMESPACE_ATTR);
		}

		return _targetNamespace;
	}
}