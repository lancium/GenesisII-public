/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.math;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class RationalNumber implements Serializable, Comparable<RationalNumber>
{
	static private Pattern _intFormat = Pattern.compile(
		"^\\s*(-)?\\s*([0-9]+)\\s*$");
	static private Pattern _fractionalFormat = Pattern.compile(
		"^\\s*(-)?\\s*([0-9]+)/([0-9]+)\\s*$");
	static private Pattern _complexFormat = Pattern.compile(
		"^\\s*(-)?\\s*([0-9]+)\\s+([0-9]+)/([0-9]+)\\s*$");
	
	static final long serialVersionUID = 0;
	
	private int _denominator;
	private int _numerator;
	
	public RationalNumber(int whole, int numerator, int denominator)
	{
		this(denominator * whole + numerator, denominator);
	}
	
	public RationalNumber(int numerator, int denominator)
	{
		if (denominator == 0)
			throw new IllegalArgumentException("Denominator cannot be 0.");
		
		int gcd = euclidsAlgorithm(numerator, denominator);
		_numerator = numerator / gcd;
		_denominator = denominator / gcd;
		
		if (_denominator < 0)
		{
			_denominator *= -1;
			_numerator *= -1;
		}
	}
	
	public RationalNumber(int whole)
	{
		this(whole, 1);
	}
	
	static private int euclidsAlgorithm(int num, int den)
	{
		int remainder;
		
		while (true)
		{
			remainder = num % den;
			if (remainder == 0)
				break;
			num = den;
			den = remainder;
		}
		
		return den;
	}
	
	public int compareTo(RationalNumber other)
	{
		if (_numerator == other._numerator && _denominator == other._denominator)
			return 0;
		
		if (toDouble() < other.toDouble())
			return -1;
		
		return 1;
	}
	
	public boolean equals(RationalNumber other)
	{
		return _numerator == other._numerator 
			&& _denominator == other._denominator;
	}
	
	public boolean equals(Object other)
	{
		return equals((RationalNumber)other);
	}
	
	public int hashCode()
	{
		return _numerator ^ _denominator;
	}
	
	public double toDouble()
	{
		return (double)_numerator / (double)_denominator;
	}
	
	public int getWholePart()
	{
		return _numerator / _denominator;
	}
	
	public int getNumeratorPart()
	{
		return Math.abs(_numerator - (getWholePart() * _denominator));
	}
	
	public int getDenominatorPart()
	{
		return _denominator;
	}
	
	public String toString()
	{
		int whole = getWholePart();
		int num = getNumeratorPart();
		int den = getDenominatorPart();
		
		StringBuffer buf = new StringBuffer();
		
		if (whole != 0)
		{
			buf.append(Integer.toString(whole));
			if (num != 0)
				buf.append(" ");
		} else if (_numerator < 0)
		{
			buf.append("-");
		}
		
		if (num != 0)
			buf.append(Integer.toString(num) + "/" + den);
		
		return buf.toString();
	}
	
	static public RationalNumber add(RationalNumber one, RationalNumber two)
	{
		int num1 = one._numerator;
		int num2 = two._numerator;
		int den1 = one._denominator;
		int den2 = two._denominator;
		
		if (den1 != den2)
		{
			num1 *= den2;
			num2 *= den1;
			den1 *= den2;
		}
		
		return new RationalNumber(num1 + num2, den1);
	}
	
	static public RationalNumber subtract(RationalNumber one, RationalNumber two)
	{
		int num1 = one._numerator;
		int num2 = two._numerator;
		int den1 = one._denominator;
		int den2 = two._denominator;
		
		if (den1 != den2)
		{
			num1 *= den2;
			num2 *= den1;
			den1 *= den2;
		}
		
		return new RationalNumber(num1 - num2, den1);
	}
	
	static public RationalNumber multiply(RationalNumber one, RationalNumber two)
	{
		return new RationalNumber(one._numerator * two._numerator,
			one._denominator * two._denominator);
	}
	
	static public RationalNumber divide(RationalNumber one, RationalNumber two)
	{
		return new RationalNumber(one._numerator * two._denominator,
			one._denominator * two._numerator);
	}
	
	static public RationalNumber fromString(String str)
	{
		Matcher m;
		
		str = str.trim();
		if (str.length() == 0)
			return new RationalNumber(0);
		
		m = _intFormat.matcher(str);
		if (m.matches())
		{
			int num = Integer.parseInt(m.group(2));
			if (m.group(1) != null)
				return new RationalNumber(-1 * num);
			else
				return new RationalNumber(num);
				
		} else
		{
			m = _fractionalFormat.matcher(str);
			if (m.matches())
			{
				int num = Integer.parseInt(m.group(2));
				int denom = Integer.parseInt(m.group(3));
				
				if (m.group(1) != null)
					return new RationalNumber(-1 * num, denom);
				else
					return new RationalNumber(num, denom);
			} else
			{
				m = _complexFormat.matcher(str);
				if (m.matches())
				{
					int whole = Integer.parseInt(m.group(2));
					int num = Integer.parseInt(m.group(3));
					int denom = Integer.parseInt(m.group(4));
					
					if (m.group(1) != null)
						return new RationalNumber(-1 * whole, num, denom);
					else
						return new RationalNumber(whole, num, denom);
				} else
				{
					throw new NumberFormatException(
						"Number \"" + str + "\" is not valid.");
				}
			}
		}
	}
}
