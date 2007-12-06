package edu.virginia.vcgr.ogrsh.server.dir;

import java.io.IOException;

import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;

public class TimeValStructure implements IPackable
{
	private long _seconds;
	private long _microseconds;
	
	public TimeValStructure()
	{
		this(0L, 0L);
	}
	
	public TimeValStructure(long seconds)
	{
		this(seconds, 0L);
	}
	
	public TimeValStructure(long seconds, long microseconds)
	{
		_seconds = seconds;
		_microseconds = microseconds;
	}
	
	public TimeValStructure(IOGRSHReadBuffer buffer) throws IOException
	{
		unpack(buffer);
	}
	
	public long getSeconcds()
	{
		return _seconds;
	}
	
	public void setSeconds(long seconds)
	{
		_seconds = seconds;
	}
	
	public long getMicroSeconds()
	{
		return _microseconds;
	}
	
	public void setMicroSeconds(long microSeconds)
	{
		_microseconds = microSeconds;
	}
	
	@Override
	public void pack(IOGRSHWriteBuffer buffer) throws IOException
	{
		buffer.writeObject(_seconds);
		buffer.writeObject(_microseconds);
	}

	@Override
	public void unpack(IOGRSHReadBuffer buffer) throws IOException
	{
		_seconds = (Long)buffer.readObject();
		_microseconds = (Long)buffer.readObject();
	}
}