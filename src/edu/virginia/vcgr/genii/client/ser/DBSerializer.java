package edu.virginia.vcgr.genii.client.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;
import org.xml.sax.InputSource;


public class DBSerializer
{
	static public Blob toBlob(Object obj)
		throws IOException
	{
		try
		{
			return new SerialBlob(serialize(obj));
		}
		catch (SQLException sqe)
		{
			throw new IOException(sqe.getLocalizedMessage());
		}
	}
	
	static public Object fromBlob(Blob b)
		throws IOException, ClassNotFoundException
	{
		InputStream in = null;
		ObjectInputStream oin = null;
		
		try
		{
			oin = new ObjectInputStream(in = b.getBinaryStream());
			return oin.readObject();
		}
		catch (SQLException sqe)
		{
			throw new IOException(sqe.getLocalizedMessage());
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public byte[] serialize(Object obj)
		throws IOException
	{
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null; 
		
		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
		}
		finally
		{
			StreamUtils.close(oos);
		}
		
		return baos.toByteArray();
	}
	
	static public Object deserialize(byte []data)
		throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = null;
		
		try
		{
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			return ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}
	
	static private QName _SERIALIZE_NAME =
		new QName("http://tempuri.org", "serialized-entity");
	
	static public byte[] xmlSerialize(Object obj)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		OutputStreamWriter writer = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, obj, _SERIALIZE_NAME);
			writer.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(writer);
		}
	}
	
	@SuppressWarnings("unchecked")
	static public <Type> Type xmlDeserialize(Class<Type> type,
		byte[] data) throws IOException
	{
		ByteArrayInputStream bais = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			return (Type)ObjectDeserializer.deserialize(new InputSource(bais), type);
		}
		finally
		{
			StreamUtils.close(bais);
		}
	}
	
	static public Blob xmlToBlob(Object obj)
		throws IOException
	{
		try
		{
			byte []data = xmlSerialize(obj);
			return new SerialBlob(data);
		}
		catch (SQLException sqe)
		{
			throw new IOException(sqe.getLocalizedMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	static public <Type> Type xmlFromBlob(Class<Type> type, Blob blob) 
		throws IOException, ClassNotFoundException
	{
		InputStream in = null;
		
		try
		{
			in = blob.getBinaryStream();
			return (Type)ObjectDeserializer.deserialize(new InputSource(in), type);
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to deserialize object.", sqe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}