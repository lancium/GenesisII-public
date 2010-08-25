package edu.virginia.vcgr.genii.container.resolver;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.axis.types.URI.MalformedURIException;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

@XmlAccessorType(XmlAccessType.NONE)
class SimpleResolverTerminateUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "epi", required = true, nillable = false)
	private URI _epi = null;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "version", required = true, nillable = false)
	private int _version = -1;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "guid", required = true, nillable = false)
	private String _guid = null;
	
	@SuppressWarnings("unused")
	private SimpleResolverTerminateUserData()
	{
	}
	
	public SimpleResolverTerminateUserData(URI epi, int version, String guid)
	{
		_epi = epi;
		_version = version;
		_guid = guid;
	}
	
	public org.apache.axis.types.URI getEPI()
	{
		try
		{
			return new org.apache.axis.types.URI(_epi.toString());
		} 
		catch (MalformedURIException e)
		{
			throw new RuntimeException(
				"This shouldn't have happend.", e);
		}
	}
	
	public int getVersion()
	{
		return _version;
	}

	public String getSubscriptionGUID()
	{
		return _guid;
	}
}