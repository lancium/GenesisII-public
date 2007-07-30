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
package org.morgan.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
  * Creates a random token (a random string of characters like a GUID).
  *
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class RandomToken implements Serializable
{
	static final long serialVersionUID = 0;
	
	static private final int _DEFAULT_NUMBER_OF_BYTES = 8;
	static private Random _defaultGenerator = new Random();
	
	protected byte[] _token;
	
	public RandomToken(int numBytes)
	{
		if (numBytes <= 0)
			throw new IllegalArgumentException("Must have a positive number of bytes.");
		
		_token = new byte[numBytes];
		_defaultGenerator.nextBytes(_token);
	}
	
	public RandomToken(byte []token)
	{
		if (token == null || token.length == 0)
			throw new IllegalArgumentException(
				"token argument must be non-null with at least one element in it.");
		
		_token = token;
	}
	
	public RandomToken()
	{
		this(_DEFAULT_NUMBER_OF_BYTES);
	}
	
	public byte[] getBytes()
	{
		return _token;
	}
	
	public boolean equals(RandomToken other)
	{
		return Arrays.equals(_token, other._token);
	}
	
	public boolean equals(Object other)
	{
		return equals((RandomToken)other);
	}
	
	public int hashCode()
	{
		int h = 0;
		
		for (int lcv = 0; lcv < _token.length; lcv++)
		{
			h <<= 3;
			h ^= _token[lcv];
		}
		
		return h;
	}
	
	public String toString(boolean capitalHex)
	{
		char alpha = (capitalHex ? 'A' : 'a');
		StringBuffer buffer = new StringBuffer();
		for (byte b : _token)
		{
			int index = (b >> 4) & 0x0F;
			if (index < 10)
				buffer.append((char)('0' + index));
			else
				buffer.append((char)(alpha + (index - 10)));
			
			index = (b & 0x0F);
			if (index < 10)
				buffer.append((char)('0' + index));
			else
				buffer.append((char)(alpha + (index - 10)));
		}
		
		return buffer.toString();
	}
	
	public String toString()
	{
		return toString(false);
	}
}
