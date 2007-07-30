package edu.virginia.vcgr.genii.wsdl;

import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WsdlOperation extends AbstractXMLComponent implements
		IXMLComponent
{
	private String _name;
	private WsdlInputOutput _input = null;
	private WsdlInputOutput _output = null;
	private HashMap<String, WsdlFault> _faults = new HashMap<String, WsdlFault>();
	
	public WsdlOperation(WsdlSourcePath sourcePath, IXMLComponent parent, 
		Node wsdlOperationNode) throws WsdlException
	{
		super(sourcePath, parent, wsdlOperationNode);
		
		_name = WsdlUtils.getAttribute(wsdlOperationNode.getAttributes(), 
			WsdlConstants.NAME_ATTR, true);
		
		NodeList children = _representedNode.getChildNodes();
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childName = WsdlUtils.getQName(child);
				
				if (childName.equals(WsdlConstants.INPUT_QNAME))
				{
					_input = new WsdlInputOutput(
						_sourcePath, this, child);
				} else if (childName.equals(WsdlConstants.OUTPUT_QNAME))
				{
					_output = new WsdlInputOutput(
						_sourcePath, this, child);
				} else if (childName.equals(WsdlConstants.FAULT_QNAME))
				{
					WsdlFault fault = new WsdlFault(_sourcePath, this, child);
					_faults.put(fault.getFaultName(), fault);
				}
			}
		}
	}
	
	public String getOperationName()
	{
		return _name;
	}
	
	public WsdlInputOutput getInput()
	{
		return _input;
	}
	
	public WsdlInputOutput getOutput()
	{
		return _output;
	}
	
	public Collection<WsdlFault> getFaults()
	{
		return _faults.values();
	}
}