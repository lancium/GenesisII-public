package edu.virginia.vcgr.fsii.file;

import java.util.BitSet;

/**
 * These are the flags that can be used to open a file. They specify conditions that affect that
 * creation such as whether to truncate the file, create it, append, etc.
 * 
 * @author mmm2a
 */
public class OpenFlags
{
	static final public int CREATE = 0;
	static final public int APPEND = 1;
	static final public int TRUNCATE = 2;
	static final public int EXCLUSIVE = 3;

	private BitSet _bits = new BitSet();

	public OpenFlags(boolean isCreate, boolean isAppend, boolean isTruncate, boolean isExclusive)
	{
		_bits.set(CREATE, isCreate);
		_bits.set(APPEND, isAppend);
		_bits.set(TRUNCATE, isTruncate);
		_bits.set(EXCLUSIVE, isExclusive);
	}

	public boolean isCreate()
	{
		return _bits.get(CREATE);
	}

	public boolean isAppend()
	{
		return _bits.get(APPEND);
	}

	public boolean isTruncate()
	{
		return _bits.get(TRUNCATE);
	}

	public boolean isExclusive()
	{
		return _bits.get(EXCLUSIVE);
	}
}