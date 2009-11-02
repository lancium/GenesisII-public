package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class NamespaceBinding implements Serializable
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "ns", required = true)
	private String _namespaceURI;
	
	@XmlAttribute(name = "prefix", required = true)
	private String _prefix;
	
	public NamespaceBinding(String namespaceURI, String prefix)
	{
		_namespaceURI = namespaceURI;
		_prefix = prefix;
	}
	
	public NamespaceBinding()
	{
		this(null, null);
	}
	
	final public String namespaceURI()
	{
		return _namespaceURI;
	}
	
	final public String prefix()
	{
		return _prefix;
	}
}