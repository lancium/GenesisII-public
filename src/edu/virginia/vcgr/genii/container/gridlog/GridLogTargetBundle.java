package edu.virginia.vcgr.genii.container.gridlog;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;

public class GridLogTargetBundle
{
	private EndpointReferenceType _epr;
	private GridLogTarget _target;
	
	public GridLogTargetBundle(EndpointReferenceType epr, GridLogTarget target)
	{
		_epr = epr;
		_target = target;
	}
	
	final public EndpointReferenceType epr()
	{
		return _epr;
	}
	
	final public GridLogTarget target()
	{
		return _target;
	}
}