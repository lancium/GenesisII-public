package edu.virginia.vcgr.genii.container.deployer;

import java.sql.Timestamp;

public class DeployFacet
{
	private String _componentID;
	private Timestamp _lastModified;

	public DeployFacet(String componentID, Timestamp lastModified)
	{
		_componentID = componentID;
		_lastModified = lastModified;
	}

	public String getComponentID()
	{
		return _componentID;
	}

	public Timestamp getLastModified()
	{
		return _lastModified;
	}

	public boolean equals(DeployFacet other)
	{
		return (_componentID.equals(other._componentID)) && (_lastModified.equals(other._lastModified));
	}

	public boolean equals(Object other)
	{
		return equals((DeployFacet) other);
	}

	public int hashCode()
	{
		return _componentID.hashCode() ^ _lastModified.hashCode();
	}
}