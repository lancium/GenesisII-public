package edu.virginia.vcgr.genii.client.iterator;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;

public class IteratorConstructionParameters extends ConstructionParameters
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "iterator-id", required = true)
	private String _iteratorID = null;
	
	public IteratorConstructionParameters(String iteratorID)
	{
		_iteratorID = iteratorID;
	}
	
	public IteratorConstructionParameters()
	{
		this(null);
	}
	
	final public String iteratorID()
	{
		return _iteratorID;
	}
}