package edu.virginia.vcgr.fsii.security;

import java.util.BitSet;

public class Permissions
{
	private BitSet _bits = new BitSet(PermissionBits.values().length);

	private void setBit(String sRep, int pos, char acceptedValue, PermissionBits bit)
	{
		char c = sRep.charAt(pos);
		if (c == acceptedValue)
			_bits.set(bit.ordinal(), true);
		else if (c != '-')
			throw new IllegalArgumentException(String.format("The bit at position %d must be either %c or -.", pos,
				acceptedValue));
	}

	public Permissions()
	{
	}

	public Permissions(String stringRep)
	{
		if (stringRep.length() != 6)
			throw new IllegalArgumentException("Permission strings MUST be 6 characters long.");

		setBit(stringRep, 0, 'r', PermissionBits.OWNER_READ);
		setBit(stringRep, 1, 'w', PermissionBits.OWNER_WRITE);
		setBit(stringRep, 2, 'x', PermissionBits.OWNER_EXECUTE);
		setBit(stringRep, 3, 'r', PermissionBits.EVERYONE_READ);
		setBit(stringRep, 4, 'w', PermissionBits.EVERYONE_WRITE);
		setBit(stringRep, 5, 'x', PermissionBits.EVERYONE_EXECUTE);
	}

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
		return String.format("%c%c%c%c%c%c", toChar(PermissionBits.OWNER_READ, 'r'), toChar(PermissionBits.OWNER_WRITE, 'w'),
			toChar(PermissionBits.OWNER_EXECUTE, 'x'), toChar(PermissionBits.EVERYONE_READ, 'r'),
			toChar(PermissionBits.EVERYONE_WRITE, 'w'), toChar(PermissionBits.EVERYONE_EXECUTE, 'x'));
	}
}