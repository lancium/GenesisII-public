package edu.virginia.vcgr.genii.container.rns;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rns.RNSUtilities;

/**
 * To ease the transition of RNS to 1.1 in the system, I created this class to
 * mimic the behavior of the old RNS EntryType type.  Its merely a transitional
 * class that has a similar interface to the old one.
 * 
 * @author morgan
 */
final public class LegacyEntryType
{
	private String _entryName;
	private MessageElement []_any;
	private EndpointReferenceType _epr;
	
	public LegacyEntryType(String entryName, MessageElement []any,
		EndpointReferenceType epr)
	{
		_entryName = entryName;
		_any = any;
		_epr = epr;
	}
	
	public LegacyEntryType(String entryName, RNSMetadataType mdt,
		EndpointReferenceType epr)
	{
		this(entryName, (mdt == null) ? null : mdt.get_any(), epr);
	}
	
	public LegacyEntryType(RNSEntryType entry)
	{
		this(entry.getEntryName(), entry.getMetadata(), entry.getEndpoint());
	}
	
	public LegacyEntryType(RNSEntryResponseType entry)
	{
		this(entry.getEntryName(), entry.getMetadata(), entry.getEndpoint());
	}
	
	public LegacyEntryType()
	{
		this(null, (RNSMetadataType)null, null);
	}
	
	final public String getEntry_name()
	{
		return _entryName;
	}
	
	final public void setEntry_name(String entry_name)
	{
		_entryName = entry_name;
	}
	
	final public MessageElement[] get_any()
	{
		return _any;
	}
	
	final public void set_any(MessageElement[] any)
	{
		_any = any;
	}
	
	final public EndpointReferenceType getEntry_reference()
	{
		return _epr;
	}
	
	final public void setEntry_reference(EndpointReferenceType entry_reference)
	{
		_epr = entry_reference;
	}
	
	final public RNSEntryType toEntryType()
	{
		return new RNSEntryType(_epr,
			RNSUtilities.createMetadata(_epr, _any),
			_entryName);
	}
	
	final public RNSEntryResponseType toEntryResponseType()
	{
		return new RNSEntryResponseType(_epr,
			RNSUtilities.createMetadata(_epr, _any),
			null, _entryName);
	}
}