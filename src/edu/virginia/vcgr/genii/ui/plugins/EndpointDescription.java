package edu.virginia.vcgr.genii.ui.plugins;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.EndpointType;

final public class EndpointDescription
{
	private TypeInformation _typeInformation;
	private EndpointType _endpointType;
	private boolean _isLocal;

	public EndpointDescription(RNSPath path) throws RNSPathDoesNotExistException
	{
		this(path.getEndpoint());
	}

	public EndpointDescription(EndpointReferenceType epr)
	{
		_typeInformation = new TypeInformation(epr);
		_endpointType = EndpointType.determineType(_typeInformation);
		_isLocal = EndpointType.isLocal(epr);
	}

	public EndpointDescription(TypeInformation typeInformation, EndpointType endpointType, boolean isLocal)
	{
		_typeInformation = typeInformation;
		_endpointType = endpointType;
		_isLocal = isLocal;
	}

	final public TypeInformation typeInformation()
	{
		return _typeInformation;
	}

	final public EndpointType endpointType()
	{
		return _endpointType;
	}

	final public boolean isLocal()
	{
		return _isLocal;
	}
}