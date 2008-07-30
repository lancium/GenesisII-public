package edu.virginia.vcgr.genii.client.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class InMemorySerializedContextResolver implements IContextResolver
{
	private byte []_storedContext;
	
	public InMemorySerializedContextResolver()
	{
		this(null);
	}
	
	private InMemorySerializedContextResolver(byte []newData)
	{
		_storedContext = newData;
	}
	
	@Override
	public ICallingContext load() throws IOException, FileNotFoundException
	{
		if (_storedContext == null)
			return null;
		
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(_storedContext);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ICallingContext)ois.readObject();
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new IOException("Couldn't find class for deserialization.", cnfe);
		}
	}

	@Override
	public void store(ICallingContext ctxt) throws FileNotFoundException,
			IOException
	{
		if (ctxt == null)
			_storedContext = null;
		else
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(ctxt);
			oos.flush();
			_storedContext = baos.toByteArray();
		}
	}
	
	public Object clone()
	{
		if (_storedContext == null)
			return new InMemorySerializedContextResolver(null);
		
		return new InMemorySerializedContextResolver(
			Arrays.copyOf(_storedContext, _storedContext.length));
	}
}