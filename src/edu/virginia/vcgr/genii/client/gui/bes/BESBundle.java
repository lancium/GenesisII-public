package edu.virginia.vcgr.genii.client.gui.bes;

import org.ws.addressing.EndpointReferenceType;

public class BESBundle
{
	private String _path;
	private EndpointReferenceType _epr;
	
	public BESBundle(String path, EndpointReferenceType epr)
	{
		_path = path;
		_epr = epr;
	}
	
	final public String path()
	{
		return _path;
	}
	
	final public EndpointReferenceType epr()
	{
		return _epr;
	}
	
	@Override
	public String toString()
	{
		return _path;
	}
}