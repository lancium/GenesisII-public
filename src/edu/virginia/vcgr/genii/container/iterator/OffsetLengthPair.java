package edu.virginia.vcgr.genii.container.iterator;

class OffsetLengthPair
{
	private long _offset;
	private long _length;
	
	public OffsetLengthPair(long offset, long length)
	{
		_offset = offset;
		_length = length;
	}
	
	final public long offset()
	{
		return _offset;
	}
	
	final public long length()
	{
		return _length;
	}
}