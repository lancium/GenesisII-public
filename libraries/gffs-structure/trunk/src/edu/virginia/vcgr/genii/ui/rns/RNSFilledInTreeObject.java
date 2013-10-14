package edu.virginia.vcgr.genii.ui.rns;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.EndpointType;

public class RNSFilledInTreeObject extends DefaultRNSTreeObject
{
	private RNSPath _path;
	private EndpointReferenceType _epr;
	private TypeInformation _typeInformation;
	private EndpointType _endpointType;
	private boolean _isLocal;

	public RNSFilledInTreeObject(RNSPath path, EndpointReferenceType epr, TypeInformation typeInformation,
		EndpointType endpointType)
	{
		super(RNSTreeObjectType.ENDPOINT_OBJECT);

		if (path == null)
			throw new IllegalArgumentException("Path cannot be null.");
		if (epr == null) {
			try {
				epr = path.getEndpoint();
			} catch (RNSPathDoesNotExistException rpdnee) {
				throw new RuntimeException("Unable to get endpoint from path.", rpdnee);
			}
		}

		if (typeInformation == null)
			typeInformation = new TypeInformation(epr);
		if (endpointType == null)
			endpointType = EndpointType.determineType(typeInformation);

		_path = path;
		_epr = epr;
		_typeInformation = typeInformation;
		_endpointType = endpointType;
		_isLocal = EndpointType.isLocal(epr);
	}

	public RNSFilledInTreeObject(RNSPath path, EndpointReferenceType epr, TypeInformation typeInformation)
	{
		this(path, epr, typeInformation, null);
	}

	public RNSFilledInTreeObject(RNSPath path, EndpointReferenceType epr)
	{
		this(path, epr, null, null);
	}

	public RNSFilledInTreeObject(RNSPath path) throws RNSPathDoesNotExistException
	{
		this(path, path.getEndpoint(), null, null);
	}

	final public boolean isLocal()
	{
		return _isLocal;
	}

	final public RNSPath path()
	{
		return _path;
	}

	final public EndpointReferenceType endpoint()
	{
		return _epr;
	}

	final public TypeInformation typeInformation()
	{
		return _typeInformation;
	}

	final public EndpointType endpointType()
	{
		return _endpointType;
	}

	@Override
	final public boolean allowsChildren()
	{
		return _typeInformation.isRNS();
	}

	@Override
	final public String toString()
	{
		return _path.getName();
	}

	final boolean isLocal(ApplicationContext appContext)
	{
		return appContext.isLocal(_epr);
	}
}