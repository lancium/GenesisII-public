package edu.virginia.vcgr.genii.client.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

import org.morgan.util.io.StreamUtils;

public class SerializedContext implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private byte[] _data;
	private HashMap<String, Serializable> _transientProperties;
	
	public SerializedContext(byte []data, HashMap<String, Serializable> transientProperties)
	{
		_data = data;
		_transientProperties = transientProperties;
	}
	
	public SerializedContext()
	{
		_data = null;
	}
	
	public Object readResolve() throws ObjectStreamException
	{
		ByteArrayInputStream bais = null;
		
		try
		{
			bais = new ByteArrayInputStream(_data);
			CallingContextImpl context = (CallingContextImpl)ContextStreamUtils.load(bais);
			context.setTransientProperties(_transientProperties);
			return context;
		}
		catch (IOException ioe)
		{
			throw new NotSerializableException("SerializedContext");
		}
		finally
		{
			StreamUtils.close(bais);
		}
	}
}