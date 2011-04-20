package edu.virginia.vcgr.genii.client.queue;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;

@XmlAccessorType(XmlAccessType.NONE)
public class QueueConstructionParameters
	extends ConstructionParameters implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final public String QUEUE_CONS_PARMS_NS =
		"http://vcgr.cs.virginia.edu/construction-parameters/queue";
	
	private ResourceOverrides _resourceOverrides = new ResourceOverrides();
		
	public QueueConstructionParameters(ResourceOverrides resourceOverrides)
	{
		setResourceOverrides(resourceOverrides);
	}
	
	public QueueConstructionParameters()
	{
		this(null);
	}
	
	@XmlElement(namespace = QUEUE_CONS_PARMS_NS, name = "resource-overrides",
			required = false, nillable = true)
	final public ResourceOverrides getResourceOverrides()
	{
		return _resourceOverrides;
	}
	
	final public void setResourceOverrides(ResourceOverrides resourceOverrides)
	{
		if (resourceOverrides == null)
			resourceOverrides = new ResourceOverrides();
		
		_resourceOverrides = resourceOverrides;
	}
}