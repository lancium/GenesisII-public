package edu.virginia.vcgr.genii.container.q2.besinfo;

import java.sql.Connection;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InformationEndpoint;
import edu.virginia.vcgr.genii.container.q2.IBESPortTypeResolver;

public class BESEndpoint implements InformationEndpoint
{
	private long _besID;
	private String _besName;
	private String _queueID;
	private IBESPortTypeResolver _portTypeResolver;
	
	GeniiBESPortType getClientStub(Connection connection)
		throws Throwable
	{
		return _portTypeResolver.createClientStub(connection, _besID);
	}
	
	public BESEndpoint(String queueID, long besID, String besName,
		IBESPortTypeResolver portTypeResolver)
	{
		_queueID = queueID;
		_besID = besID;
		_besName = besName;
		_portTypeResolver = portTypeResolver;
	}
	
	public long getBESID()
	{
		return _besID;
	}
	
	public boolean equals(BESEndpoint other)
	{
		return _besID == other._besID;
	}
	
	@Override 
	public boolean equals(Object other)
	{
		if (other instanceof BESEndpoint)
			return equals((BESEndpoint)other);
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return (int)_besID;
	}
	
	@Override
	public String toString()
	{
		return String.format("[%d] %s@%s",
			_besID, _besName, _queueID);
	}
}