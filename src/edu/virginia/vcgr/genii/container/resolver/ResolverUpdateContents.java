package edu.virginia.vcgr.genii.container.resolver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.container.sync.VersionVector;

@XmlRootElement(namespace = ResolverUpdateContents.RESOLVER_NAMESPACE,
		name = "ResolverUpdateContents")
public class ResolverUpdateContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;
	
	static public final String RESOLVER_NAMESPACE =
		"http://vcgr.cs.virginia.edu/genii/genii-resolver";
	
	@XmlElement(namespace = RESOLVER_NAMESPACE,
			name = "targetEPI", nillable = true, required = true)
	private URI _targetEPI;
	
	@XmlElement(namespace = RESOLVER_NAMESPACE,
			name = "targetID", nillable = false, required = true)
	private int _targetID;
	
	@XmlElement(namespace = RESOLVER_NAMESPACE,
			name = "entryReference", nillable = true, required = true)
	private byte[] _entryReference;
	
	@XmlElement(namespace = RESOLVER_NAMESPACE,
			name = "version")
	private VersionVector _versionVector;
	
	protected ResolverUpdateContents()
	{
	}
	
	public ResolverUpdateContents(int targetID, EndpointReferenceType entryReference,
			VersionVector versionVector)
		throws ResourceException
	{
		_targetID = targetID;
		if (entryReference != null)
			_entryReference = EPRUtils.toBytes(entryReference);
		_versionVector = versionVector;
	}
	
	public ResolverUpdateContents(URI targetEPI, int targetID,
			VersionVector versionVector)
		throws ResourceException
	{
		_targetEPI = targetEPI;
		_targetID = targetID;
		_versionVector = versionVector;
	}

	final public URI targetEPI()
	{
		return _targetEPI;
	}
	
	final public int targetID()
	{
		return _targetID;
	}

	final public EndpointReferenceType entryReference()
		throws ResourceException
	{
		if (_entryReference == null)
			return null;
		return EPRUtils.fromBytes(_entryReference);
	}
	
	final public VersionVector versionVector()
	{
		return _versionVector;
	}
}
