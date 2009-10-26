package edu.virginia.vcgr.genii.client.gridlog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.gridlog.GridLogPortType;

public class GridLogTarget implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _uri;
	private String _containerID;
	private String _loggerID;
	
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.writeUTF(_uri);
		out.writeUTF(_containerID);
		out.writeUTF(_loggerID);
	}
	
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		_uri = in.readUTF();
		_containerID = in.readUTF();
		_loggerID = in.readUTF();
	}
	
	@SuppressWarnings("unused")
	private void readObjectNoData() 
		throws ObjectStreamException
	{
		throw new StreamCorruptedException(
			"Unable to deserialize grid log target.");
	}

	public GridLogTarget(String uri, String containerID, String loggerID)
	{
		_uri = uri;
		_containerID = containerID;
		_loggerID = loggerID;
	}
	
	public boolean equals(GridLogTarget other)
	{
		return _uri.equals(other._uri) &&
			_containerID.equals(other._containerID) &&
			_loggerID.equals(other._loggerID);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof GridLogTarget)
			return equals((GridLogTarget)other);
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _loggerID.hashCode();
	}
	
	final public String uri()
	{
		return _uri;
	}
	
	final public String loggerID()
	{
		return _loggerID;
	}
	
	final public String containerID()
	{
		return _containerID;
	}
	
	final public GridLogPortType connect() 
		throws ResourceException, GenesisIISecurityException
	{
		return ClientUtils.createProxy(GridLogPortType.class, 
			new EndpointReferenceType(
				new AttributedURIType(_uri), new ReferenceParametersType(),
				new MetadataType(), null), null);
	}
}