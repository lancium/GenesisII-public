package edu.virginia.vcgr.genii.client.resource;

import javax.xml.namespace.QName;

public class PortType
{
	private QName _portTypeName;
	private String _description;
	private int _displayRank;

	// the port type admin knows how to regenerate port types from bit vectors.
	private static PortTypeAdministrator portAdmin = null;

	synchronized public static PortTypeAdministrator portTypeFactory()
	{
		if (portAdmin == null) {
			portAdmin = new PortTypeAdministrator();
		}
		return portAdmin;
	}

	/**
	 * not for general use. (only public to allow PortTypeAdministrator to access this.)
	 */
	public PortType(QName portTypeName, int displayRank, String description)
	{
		_portTypeName = portTypeName;
		_displayRank = displayRank;
		_description = (description == null) ? "" : description;
	}

	public QName getQName()
	{
		return _portTypeName;
	}

	public int getDisplayRank()
	{
		return _displayRank;
	}

	public String getDescription()
	{
		return _description;
	}

	public String toString()
	{
		return _portTypeName.toString();
	}

	public boolean equals(PortType other)
	{
		return _portTypeName.equals(other._portTypeName);
	}

	public boolean equals(Object other)
	{
		if (other instanceof PortType)
			return equals((PortType) other);
		return false;
	}

	public int hashCode()
	{
		return _portTypeName.hashCode();
	}
}
