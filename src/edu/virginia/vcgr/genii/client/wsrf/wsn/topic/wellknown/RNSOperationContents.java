package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.container.sync.VersionVector;

@XmlRootElement(namespace = RNSConstants.GENII_RNS_NS,
	name = "RNSOperationContents")
public class RNSOperationContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
			name = "operation", nillable = false, required = true)
	private String _operation;
		
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
			name = "EntryName", nillable = false, required = true)
	private String _entryName;
	
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
			name = "EntryReference", nillable = false, required = true)
	private byte[] _entryReference;
	
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
			name = "versionVector")
	private VersionVector _versionVector;

	protected RNSOperationContents()
	{
	}
	
	public RNSOperationContents(String operation, String entryName, 
			EndpointReferenceType entryReference, VersionVector versionVector)
		throws ResourceException
	{
		_operation = operation;
		_entryName = entryName;
		if (entryReference != null)
			_entryReference = EPRUtils.toBytes(entryReference);
		_versionVector = versionVector;
	}

	final public String operation()
	{
		return _operation;
	}
	
	final public String entryName()
	{
		return _entryName;
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
