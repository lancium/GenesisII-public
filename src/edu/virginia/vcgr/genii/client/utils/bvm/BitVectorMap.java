package edu.virginia.vcgr.genii.client.utils.bvm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class BitVectorMap<Type>
{
	private HashMap<Type, Integer> _typeToBitMap;
	private Object[] _bitPositions;

	@SuppressWarnings("unchecked")
	public BitVectorMap(Collection<Type> entries)
	{
		_typeToBitMap = new HashMap<Type, Integer>();

		_bitPositions = entries.toArray(new Object[0]);
		for (int lcv = 0; lcv < _bitPositions.length; lcv++)
			_typeToBitMap.put((Type) _bitPositions[lcv], lcv);
	}

	public String translate(Collection<Type> values)
	{
		BigInteger bi = new BigInteger("0");

		for (Type value : values) {
			Integer i = _typeToBitMap.get(value);
			if (i == null)
				throw new IllegalArgumentException("Value \"" + value + "\" is not known to this bit set.");

			bi = bi.setBit(i);
		}

		return bi.toString(16);
	}

	public String translate(Type[] values)
	{
		BigInteger bi = new BigInteger("0");

		for (Type value : values) {
			Integer i = _typeToBitMap.get(value);
			if (i == null)
				throw new IllegalArgumentException("Value \"" + value + "\" is not known to this bit set.");

			bi = bi.setBit(i);
		}

		return bi.toString(16);
	}

	public String translate(Type value)
	{
		BigInteger bi = new BigInteger("0");
		Integer i = _typeToBitMap.get(value);
		if (i == null)
			throw new IllegalArgumentException("Value \"" + value + "\" is not known to this bit set.");
		bi = bi.setBit(i);
		return bi.toString(16);
	}

	@SuppressWarnings("unchecked")
	public Collection<Type> translate(String stringRep)
	{
		Collection<Type> ret = new LinkedList<Type>();
		BigInteger bi = new BigInteger(stringRep, 16);
		int length = bi.bitLength();

		if (length > _bitPositions.length)
			throw new IllegalArgumentException("Bit vector representation exceeds known entries.");

		for (int lcv = 0; lcv < bi.bitLength(); lcv++) {
			if (bi.testBit(lcv))
				ret.add((Type) _bitPositions[lcv]);
		}

		return ret;
	}
}