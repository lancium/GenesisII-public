package edu.virginia.vcgr.genii.client.gui.browser;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class RNSEntry
{
	private String _entryName;
	private EndpointReferenceType _target;
	private TypeInformation _typeInfo;
	
	public RNSEntry(RNSPath path)
		throws RNSPathDoesNotExistException
	{
		this(path.getName(), path.getEndpoint());
	}
	
	public RNSEntry(String name, EndpointReferenceType target)
	{
		_target = target;
		_typeInfo = new TypeInformation(target);
		_entryName = name;
	}
	
	public void setName(String name)
	{
		_entryName = name;
	}
	
	public EndpointReferenceType getTarget()
	{
		return _target;
	}
	
	public TypeInformation getTypeInformation()
	{
		return _typeInfo;
	}
	
	public String toString()
	{
		return _entryName;
	}
}