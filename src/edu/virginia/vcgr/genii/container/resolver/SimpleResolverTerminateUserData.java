package edu.virginia.vcgr.genii.container.resolver;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

@XmlAccessorType(XmlAccessType.NONE)
class SimpleResolverTerminateUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
			name = "targetEPI", required = true, nillable = false)
	private String _targetEPI = null;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
			name = "targetID", required = true, nillable = false)
	private int _targetID = -1;
	
	@SuppressWarnings("unused")
	private SimpleResolverTerminateUserData()
	{
	}
	
	public SimpleResolverTerminateUserData(URI targetEPI, int targetID)
	{
		_targetEPI = targetEPI.toString();
		_targetID = targetID;
	}
	
	public URI getTargetEPI()
	{
		try
		{
			return new URI(_targetEPI);
		}
		catch (Exception exception)
		{
			return null;
		}
	}
	
	public int getTargetID()
	{
		return _targetID;
	}
}