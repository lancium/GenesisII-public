package edu.virginia.vcgr.genii.wsdl;

import org.w3c.dom.Node;

public interface IXMLComponent
{
	public Node getRepresentedNode();
	public IXMLComponent getParent();
	
	public String findTargetNamespace();
}