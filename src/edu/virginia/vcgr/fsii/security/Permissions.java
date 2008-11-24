package edu.virginia.vcgr.fsii.security;

import java.util.BitSet;

public class Permissions
{
	private BitSet _bits = new BitSet(
		PermissionBits.values().length);
	
	public boolean isSet(PermissionBits bit)
	{
		return _bits.get(bit.ordinal());
	}
	
	public void set(PermissionBits bit, boolean set)
	{
		_bits.set(bit.ordinal(), set);
	}
	
	private char toChar(PermissionBits bit, char positiveChar)
	{
		return _bits.get(bit.ordinal()) ? positiveChar : '-';
	}
	
	@Override
	public String toString()
	{
		return String.format("%c%c%c%c%c%c",
			toChar(PermissionBits.OWNER_READ, 'r'),
			toChar(PermissionBits.OWNER_WRITE, 'w'),
			toChar(PermissionBits.OWNER_EXECUTE, 'x'),
			toChar(PermissionBits.EVERYONE_READ, 'r'),
			toChar(PermissionBits.EVERYONE_WRITE, 'w'),
			toChar(PermissionBits.EVERYONE_EXECUTE, 'x'));
	}
}