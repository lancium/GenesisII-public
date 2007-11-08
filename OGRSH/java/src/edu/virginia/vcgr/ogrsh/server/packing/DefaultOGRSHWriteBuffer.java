package edu.virginia.vcgr.ogrsh.server.packing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultOGRSHWriteBuffer implements IOGRSHWriteBuffer
{
	static private Log _logger = LogFactory.getLog(DefaultOGRSHWriteBuffer.class);
	
	static private final int _CAPACITY = 1024;
	
	private LinkedList<ByteBuffer> _buffers = new LinkedList<ByteBuffer>();
	private ByteBuffer _current;
	private ByteOrder _order;
	
	private void ensure(int size)
	{
		if (_current.remaining() < size)
		{
			_buffers.add(_current = ByteBuffer.allocate(
				(size < _CAPACITY) ? _CAPACITY : size));
			_current.order(_order);
		}
	}
	
	private void writeUTF(String str)
	{
		try
		{
			byte []data = str.getBytes("UTF-8");
			ensure(4);
			_current.putInt(data.length);
			int length = data.length;
			int offset = 0;
			int toWrite = _current.remaining();
			if (toWrite > length)
				toWrite = length;
			_current.put(data, offset, toWrite);
			length -= toWrite;
			offset += toWrite;
			if (length > 0)
			{
				ensure(length);
				_current.put(data, offset, length);
			}
		}
		catch (UnsupportedEncodingException uee)
		{
			_logger.fatal("Unexpected exception occurred.", uee);
			throw new RuntimeException("Unexpected exception.", uee);
		}
	}
	
	public DefaultOGRSHWriteBuffer(ByteOrder order)
	{
		_order = order;
		_buffers.add(_current = ByteBuffer.allocate(_CAPACITY));
		_current.order(_order);
	}
	
	public ByteBuffer compact()
	{
		int totalSize = 0;
		for (ByteBuffer bb : _buffers)
		{
			totalSize += bb.position();
		}
		
		ByteBuffer ret = ByteBuffer.allocate(totalSize);
		ret.order(_order);
		for (ByteBuffer bb : _buffers)
		{
			ByteBuffer tmp = ByteBuffer.class.cast(bb.asReadOnlyBuffer().flip());
			ret.put(tmp);
		}
		
		return ret;
	}
	
	public void writeObject(Object object) throws IOException
	{
		if (object == null)
			writeUTF("null");
		else
		{
			Class<?> cl = object.getClass();
			if (cl.isArray())
			{
				cl = cl.getComponentType();
				
				if (cl.equals(byte.class))
				{
					byte []array = byte[].class.cast(object);
					int len = array.length;
					
					writeUTF("byte-array");
					ensure(4);
					_current.putInt(len);
					ensure(len);
					_current.put(array);
					return;
				}
				
				Object []array = Object[].class.cast(object);
				int len = array.length;
				
				writeUTF("array");
				ensure(4);
				_current.putInt(len);
				
				writeUTF(cl.getName());
				for (Object obj : array)
				{
					writeObject(obj);
				}
				return;
			}
			
			writeUTF(cl.getName());
			if (cl.equals(Boolean.class))
			{
				ensure(1);
				_current.put(Boolean.class.cast(object).booleanValue() ? 
					(byte)1 : (byte)0);
			} else if (cl.equals(Byte.class))
			{
				ensure(1);
				_current.put(Byte.class.cast(object).byteValue());
			} else if (cl.equals(Character.class))
			{
				ensure(2);
				_current.putChar(Character.class.cast(object).charValue());
			} else if (cl.equals(Short.class))
			{
				ensure(2);
				_current.putShort(Short.class.cast(object).shortValue());
			} else if (cl.equals(Integer.class))
			{
				ensure(4);
				_current.putInt(Integer.class.cast(object).intValue());
			} else if (cl.equals(Long.class))
			{
				ensure(8);
				_current.putLong(Long.class.cast(object).longValue());
			} else if (cl.equals(Float.class))
			{
				ensure(4);
				_current.putFloat(Float.class.cast(object).floatValue());
			} else if (cl.equals(Double.class))
			{
				ensure(8);
				_current.putDouble(Double.class.cast(object).doubleValue());
			} else if (cl.equals(String.class))
			{
				writeUTF(String.class.cast(object));
			} else if (IPackable.class.isAssignableFrom(cl))
			{
				IPackable.class.cast(object).pack(this);
			} else
			{
				_logger.error("Don't know how to pack type \""
					+ cl.getName() + "\".");
				throw new IOException("Don't know how to pack type \"" 
					+ cl.getName() + "\".");
			}
		}
	}
}