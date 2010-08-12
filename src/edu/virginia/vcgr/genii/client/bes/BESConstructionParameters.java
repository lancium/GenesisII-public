package edu.virginia.vcgr.genii.client.bes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;

@XmlAccessorType(XmlAccessType.NONE)
public class BESConstructionParameters
	extends ConstructionParameters implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final public String BES_CONS_PARMS_NS =
		"http://vcgr.cs.virginia.edu/construction-parameters/bes";
	
	private ResourceOverrides _resourceOverrides = new ResourceOverrides();
	private NativeQueueConfiguration _nativeQueueConf = null;
	
	public BESConstructionParameters(ResourceOverrides resourceOverrides,
		NativeQueueConfiguration queueConfiguration)
	{
		setResourceOverrides(resourceOverrides);
		setNativeQueueConfiguration(queueConfiguration);
	}
	
	public BESConstructionParameters()
	{
		this(null, null);
	}
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "resource-overrides",
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
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "nativeq",
			required = false, nillable = false)
	final public NativeQueueConfiguration getNativeQueueConfiguration()
	{
		return _nativeQueueConf;
	}
	
	final public void setNativeQueueConfiguration(NativeQueueConfiguration nativeQueueConf)
	{
		_nativeQueueConf = nativeQueueConf;
	}
}