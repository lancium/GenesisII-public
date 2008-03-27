package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class ByteIORedirectionSink implements StreamRedirectionSink
{
	static final long serialVersionUID = 0L;
	
	private EndpointReferenceType _target;
	
	public ByteIORedirectionSink(EndpointReferenceType target)
	{
		_target = target;
	}
	
	@Override
	public OutputStream openSink(ExecutionContext context) throws IOException
	{
		try
		{
			return ByteIOStreamFactory.createOutputStream(_target);
		}
		catch (ConfigurationException ce)
		{
			throw new IOException(
				"Unable to open stream to remote object for redirect.", ce);
		}
	}
	
	private void writeObject(ObjectOutputStream out)
    	throws IOException
	{
		byte []data = EPRUtils.toBytes(_target);
		out.writeInt(data.length);
		out.write(data);
	}
	
	private void readObject(ObjectInputStream in)
    	throws IOException, ClassNotFoundException
	{
		int offset = 0;
		int length = in.readInt();
		byte []data = new byte[length];
		
		while (length > 0)
		{
			int read = in.read(data, offset, length);
			if (read <= 0)
				throw new IOException(
					"Unable to read bytes from serialized stream.");
			length -= read;
			offset += read;
		}
		
		_target = EPRUtils.fromBytes(data);
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() 
    	throws ObjectStreamException
	{
		throw new StreamCorruptedException("Unable to deserialize object.");
	}
}