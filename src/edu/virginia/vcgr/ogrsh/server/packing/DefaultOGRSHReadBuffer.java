package edu.virginia.vcgr.ogrsh.server.packing;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultOGRSHReadBuffer implements IOGRSHReadBuffer
{
	static private Log _logger = LogFactory.getLog(DefaultOGRSHReadBuffer.class);
	
	private ByteBuffer _source;

	private Class<?> findClass(String className) throws IOException
	{
		try
		{
			return Thread.currentThread().getContextClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException cnfe)
		{
			_logger.error("Can't deserialize type \"" + className + "\".", cnfe);
			throw new IOException("Can't deserialize type \"" + className 
				+ "\".");
		}
	}
	
	private IPackable construct(Class<? extends IPackable> type) throws IOException
	{
		Constructor<? extends IPackable> cons = null;
		
		try
		{
			try
			{
				cons = type.getConstructor(new Class[] { IOGRSHReadBuffer.class });
				return cons.newInstance(new Object[] { this });
			} catch (NoSuchMethodException nsme)
			{
				cons = type.getConstructor(new Class[0]);
				IPackable packable = cons.newInstance(new Object[0]);
				packable.unpack(this);
				return packable;
			}
		}
		catch (NoSuchMethodException nsme)
		{
			_logger.error("Unable to create instance of packable type \""
				+ type.getName() + "\".", nsme);
			throw new IOException(
				"Unable to create instance of packable type \"" 
				+ type.getName() + "\".");
		}
		catch (IllegalAccessException iae)
		{
			_logger.error("Unable to create instance of packable type \""
				+ type.getName() + "\".", iae);
			throw new IOException(
				"Unable to create instance of packable type \"" 
				+ type.getName() + "\".");
		}
		catch (InstantiationException ia)
		{
			_logger.error("Unable to create instance of packable type \""
				+ type.getName() + "\".", ia);
			throw new IOException(
				"Unable to create instance of packable type \"" 
				+ type.getName() + "\".");
		}
		catch (InvocationTargetException ite)
		{
			_logger.error("Unable to create instance of packable type \""
				+ type.getName() + "\".", ite);
			throw new IOException(
				"Unable to create instance of packable type \"" 
				+ type.getName() + "\".");
		}
	}
	
	public DefaultOGRSHReadBuffer(ByteBuffer source)
	{
		_source = source;
	}
	
	public String readUTF() throws IOException
	{
		int length = _source.getInt();
		byte []data = new byte[length];
		_source.get(data);
		return new String(data, "UTF-8");
	}

	@SuppressWarnings("unchecked")
	public Object readObject() throws IOException
	{
		String label = readUTF();
		if (label.equals("null"))
			return null;
		if (label.equals("byte-array"))
		{
			int len = _source.getInt();
			byte []data = new byte[len];
			_source.get(data);
			return data;
		}
		if (label.equals("array"))
		{
			int len = _source.getInt();
			label = readUTF();
			Class<?> componentType = findClass(label);
			Object []ret = Object[].class.cast(Array.newInstance(componentType, len));
			for (int lcv = 0; lcv < ret.length; lcv++)
			{
				ret[lcv] = readObject();
			}
			return ret;
		}
		
		Class<?> type = findClass(label);
		if (type.equals(Boolean.class))
		{
			return new Boolean(_source.get() != (byte)0);
		} else if (type.equals(Byte.class))
		{
			return new Byte(_source.get());
		} else if (type.equals(Character.class))
		{
			return new Character(_source.getChar());
		} else if (type.equals(Short.class))
		{
			return new Short(_source.getShort());
		} else if (type.equals(Integer.class))
		{
			return new Integer(_source.getInt());
		} else if (type.equals(Long.class))
		{
			return new Long(_source.getLong());
		} else if (type.equals(Float.class))
		{
			return new Float(_source.getFloat());
		} else if (type.equals(Double.class))
		{
			return new Double(_source.getDouble());
		} else if (type.equals(String.class))
		{
			return readUTF();
		} else if (IPackable.class.isAssignableFrom(type))
		{
			return construct((Class<? extends IPackable>)type);
		} else
		{
			_logger.error("Don't know how to deserialize type \"" 
				+ type.getName() + "\".");
			throw new IOException("Don't know how to deserialize type \"" 
				+ type.getName() + "\".");
		}
	}
}