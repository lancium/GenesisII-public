package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.axis.types.URI.MalformedURIException;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

@XmlAccessorType(XmlAccessType.NONE)
public class RExportResolverTerminateUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "epi", required = true)
	private URI _epi = null;
	
	protected RExportResolverTerminateUserData()
	{	
	}
	
	public RExportResolverTerminateUserData(URI epi)
	{
		_epi = epi;
	}
	
	final public org.apache.axis.types.URI getEPI()
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
}