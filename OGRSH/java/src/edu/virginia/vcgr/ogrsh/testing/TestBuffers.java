package edu.virginia.vcgr.ogrsh.testing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.TestCase;

import org.junit.Test;

import edu.virginia.vcgr.ogrsh.server.packing.DefaultOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.DefaultOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;

public class TestBuffers
{
	static public class Packable implements IPackable
	{
		private byte _byte;
		private char _char;
		private short _short;
		private int _int;
		private long _long;
		private float _float;
		private double _double;
		private String _string;
		
		public Packable()
		{
			_byte = (byte)1;
			_char = 'a';
			_short = (short)2;
			_int = 3;
			_long = (long)4;
			_float = (float)3.14;
			_double = (double)3.14;
			_string = "Hello, world.";
		}
		
		public Packable(IOGRSHReadBuffer buffer) throws IOException
		{
			unpack(buffer);
		}
		
		public void unpack(IOGRSHReadBuffer buffer) throws IOException
		{
			_byte = Byte.class.cast(buffer.readObject());
			_char = Character.class.cast(buffer.readObject());
			_short = Short.class.cast(buffer.readObject());
			_int = Integer.class.cast(buffer.readObject());
			_long = Long.class.cast(buffer.readObject());
			_float = Float.class.cast(buffer.readObject());
			_double = Double.class.cast(buffer.readObject());
			_string = String.class.cast(buffer.readObject());
		}
		
		public void pack(IOGRSHWriteBuffer buffer) throws IOException
		{
			buffer.writeObject(_byte);
			buffer.writeObject(_char);
			buffer.writeObject(_short);
			buffer.writeObject(_int);
			buffer.writeObject(_long);
			buffer.writeObject(_float);
			buffer.writeObject(_double);
			buffer.writeObject(_string);
		}
		
		public boolean equals(Packable other)
		{
			return ( 	(_byte == other._byte) &&
					 	(_char == other._char) &&
					 	(_short == other._short) &&
					 	(_int == other._int) &&
					 	(_long == other._long) &&
					 	(_float == other._float) &&
					 	(_double == other._double) &&
					 	(_string.equals(other._string)) );
		}
		
		public boolean equals(Object other)
		{
			return equals((Packable)other);
		}
	}
	
	@Test
	public void testNormalBuffers() throws IOException
	{
		Packable first = new Packable();
		DefaultOGRSHWriteBuffer writeBuffer = new DefaultOGRSHWriteBuffer(
			ByteOrder.nativeOrder());
		writeBuffer.writeObject(first);
		ByteBuffer buffer = writeBuffer.compact();
		buffer.flip();
		DefaultOGRSHReadBuffer readBuffer = new DefaultOGRSHReadBuffer(buffer);
		Packable second = Packable.class.cast(readBuffer.readObject());
		TestCase.assertEquals(first, second);
	}
	
	@Test
	public void testOverflow() throws IOException
	{
		DefaultOGRSHWriteBuffer writeBuffer = new DefaultOGRSHWriteBuffer(
			ByteOrder.nativeOrder());
		String str = "Hello number ";
		int approxLength = str.length();
		int numberTimes = 1024 / approxLength * 3 / 2;
		for (int lcv = 0; lcv < numberTimes; lcv++)
		{
			writeBuffer.writeObject(str + lcv);
		}
		
		ByteBuffer bb = writeBuffer.compact();
		bb.flip();
		DefaultOGRSHReadBuffer readBuffer = new DefaultOGRSHReadBuffer(bb);
		for (int lcv = 0; lcv < numberTimes; lcv++)
		{
			String str2 = String.class.cast(readBuffer.readObject());
			TestCase.assertEquals(str + lcv, str2);
		}
	}
}