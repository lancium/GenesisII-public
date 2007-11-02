package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class ExportDirInformation implements Externalizable
{
	static final long serialVersionUID = 0L;
	
	private WSName _rootEndpoint;
	private RNSPath _rnsPath;
	private File _localPath;
	
	public ExportDirInformation()
	{
		_rootEndpoint = null;
		_rnsPath = null;
		_localPath = null;
	}
	
	public ExportDirInformation(RNSPath rnsPath, File localPath)
		throws RNSPathDoesNotExistException
	{
		_rootEndpoint = new WSName(rnsPath.getEndpoint());
		_rnsPath = rnsPath;
		_localPath = localPath;
		if (!_rootEndpoint.isValidWSName())
		{
			throw new IllegalArgumentException("All exports MUST support WS-Naming.");
		}
	}
	
	public EndpointReferenceType getRootEndpoint()
	{
		return _rootEndpoint.getEndpoint();
	}
	
	public RNSPath getRNSPath()
	{
		return _rnsPath;
	}
	
	public File getLocalPath()
	{
		return _localPath;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		try
		{
			_rnsPath = (RNSPath)in.readObject();
			_rootEndpoint = new WSName(_rnsPath.getEndpoint());
			_localPath = (File)in.readObject();
			
			if (!_rootEndpoint.isValidWSName())
			{
				throw new IllegalArgumentException("All exports MUST support WS-Naming.");
			}
		}
		catch (RNSPathDoesNotExistException dne)
		{
			// This shouldn't happen
			throw new IOException("Unknown exception occured trying to parse an RNS path.", dne);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(_rnsPath);
		out.writeObject(_localPath);
	}
	
	public boolean equals(ExportDirInformation other)
	{
		return _rootEndpoint.equals(other._rootEndpoint);
	}
	
	@Override
	public boolean equals(Object other)
	{
		return equals((ExportDirInformation)other);
	}
	
	@Override
	public int hashCode()
	{
		return _rootEndpoint.hashCode();
	}
}
