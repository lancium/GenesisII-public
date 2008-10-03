package edu.virginia.vcgr.genii.container.cservices.besstatus;

import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class BESName
{
	private WSName _endpoint;
	private String _endpointIdentifier;
	
	public BESName(EndpointReferenceType epr)
	{
		_endpoint = new WSName(epr);
		if (_endpoint.isValidWSName())
			_endpointIdentifier = _endpoint.getEndpointIdentifier().toString();
		else
		{
			try
			{
				_endpointIdentifier = GUID.fromRandomBytes(
					EPRUtils.toBytes(epr)).toString();				
			}
			catch (ResourceException re)
			{
				_endpointIdentifier = null;
			}
		}
	}
	
	public EndpointReferenceType getEPR()
	{
		return _endpoint.getEndpoint();
	}

	public boolean equals(BESName other)
	{
		String one = _endpointIdentifier;
		String two = other._endpointIdentifier;
			
		if (one == null || two == null)
			return false;
		
		if (one.equals(two))
			return true;
		
		return false;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof BESName)
			return equals((BESName)other);
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		if (_endpointIdentifier == null)
			return 0;
		return _endpoint.hashCode();
	}
}