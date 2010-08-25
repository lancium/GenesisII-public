package edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@XmlRootElement(namespace = RNSConstants.GENII_RNS_NS,
	name = "RNSEntryAddedContents")
public class RNSEntryAddedContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
		name = "EntryName", nillable = false, required = true)
	private String _entryName;
	
	@XmlElement(namespace = RNSConstants.GENII_RNS_NS,
		name = "EntryReference", nillable = false, required = true)
	private EndpointReferenceType _entryReference;
	
	protected RNSEntryAddedContents()
	{
	}
	
	public RNSEntryAddedContents(String entryName, 
		EndpointReferenceType entryReference)
	{
		_entryName = entryName;
		_entryReference = entryReference;
	}
	
	final public String entryName()
	{
		return _entryName;
	}
	
	final public EndpointReferenceType entryReference()
	{
		return _entryReference;
	}
}