package edu.virginia.vcgr.genii.algorithm.math;

import java.util.Random;

public class SimpleRandomizer
{
	static Random _anyRand = new Random(); // static object seeds generator just once.

	/**
	 * generates a pseudo-random number between "min" and "max" inclusive.
	 */
	public static int randomInteger(int min, int max)
	{
		int randomNum = _anyRand.nextInt(max - min + 1) + min;
		return randomNum;
	}

}
