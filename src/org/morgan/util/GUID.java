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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class GUID extends RandomToken implements Serializable
{
	static private Pattern _GUID_PATTERN = Pattern.compile(
		"([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})");
	
	static final long serialVersionUID = 0;
	
	static final private int _NUM_BYTES = 16;
	
	static public GUID fromString(String str)
	{
		byte []bytes = new byte[_NUM_BYTES];
		byte b;
		Matcher m = _GUID_PATTERN.matcher(str);
		if (!m.matches())
			throw new IllegalArgumentException("String \"" + str + 
				"\" does not look like a GUID.");
		
		int byteIndex = 0;
		String simplified = m.group(1) + m.group(2) + m.group(3) + m.group(4) + m.group(5);
		simplified = simplified.toUpperCase();
		
		for (int lcv = 0; lcv < simplified.length(); lcv += 2)
		{
			int v1, v2;
			char c1 = simplified.charAt(lcv);
			char c2 = simplified.charAt(lcv + 1);
			
			if (c1 >= 'A')
				v1 = c1 - 'A' + 10;
			else
				v1 = c1 - '0';
			
			if (c2 >= 'A')
				v2 = c2 - 'A' + 10;
			else
				v2 = c2 - '0';
			
			b = (byte)(v1 & 0x0F);
			b <<= 4;
			b |= (byte)(v2 & 0x0F);
			bytes[byteIndex++] = b;
		}
		
		return new GUID(bytes);
	}
	
	public GUID()
	{
		super(_NUM_BYTES);
	}
	
	private GUID(byte []bytes)
	{
		super(bytes);
	}
	
	public String toString(boolean useCapitals)
	{
		String simple = super.toString(useCapitals);
		return simple.substring(0, 8) + "-" + simple.substring(8, 12) + "-" +
			simple.substring(12,16) + "-" + simple.substring(16, 20) + "-" +
			simple.substring(20);
	}
	
	public String toString()
	{
		return toString(true);
	}
	
	static public GUID fromRandomBytes(byte []data)
	{
		byte []seed = new byte[_NUM_BYTES];
		for (int lcv = 0; lcv < _NUM_BYTES; lcv++)
			seed[lcv] = 0;
		
		for (int lcv = 0; lcv < data.length; lcv++)
			seed[lcv % _NUM_BYTES] ^= data[lcv]; 
		
		return new GUID(seed);
	}
}
