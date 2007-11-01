package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;

public class ExportDirInformation implements Externalizable
{
	static final long serialVersionUID = 0L;
	
	private WSName _rootEndpoint;
	private String _rnsPath;
	private File _localPath;
	
	public ExportDirInformation()
	{
		_rootEndpoint = null;
		_rnsPath = null;
		_localPath = null;
	}
	
	public ExportDirInformation(EndpointReferenceType rootEndpoint, String rnsPath, File localPath)
	{
		_rootEndpoint = new WSName(rootEndpoint);
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
	
	public String getRNSPath()
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
		int size;
		byte []data;
		
		size = in.readInt();
		data = new byte[size];
		int position = 0;
		while (size > 0)
		{
			int read = in.read(data, position, size);
			if (read <= 0)
				throw new IOException("Unable to read EPR from object stream.");
			size -= read;
			position += read;
		}
		
		_rootEndpoint = new WSName(EPRUtils.fromBytes(data));
		_rnsPath = in.readUTF();
		_localPath = (File)in.readObject();
		
		if (!_rootEndpoint.isValidWSName())
		{
			throw new IllegalArgumentException("All exports MUST support WS-Naming.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		byte []data = EPRUtils.toBytes(_rootEndpoint.getEndpoint());
		
		out.writeInt(data.length);
		out.write(data);
		out.writeUTF(_rnsPath);
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
