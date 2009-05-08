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

package edu.virginia.vcgr.genii.client.security;

import java.util.StringTokenizer;

public class MessageLevelSecurityRequirements
{

	public static final int NONE = 0x00;
	public static final int SIGN = 0x01;
	public static final int ENCRYPT = 0x02;
	public static final int WARN = 0x04;

	private final int _value;

	public MessageLevelSecurityRequirements()
	{
		_value = NONE;
	}

	public MessageLevelSecurityRequirements(int value)
	{
		_value = value;
	}

	public MessageLevelSecurityRequirements(String value)
	{

		int temp = NONE;
		if (value != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(value, "|");
			while (tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				if (token.equals("NONE"))
				{
				}
				else if (token.equals("SIGN"))
				{
					temp = temp | SIGN;
				}
				else if (token.equals("ENCRYPT"))
				{
					temp = temp | ENCRYPT;
				}
				else if (token.equals("WARN"))
				{
					temp = temp | WARN;
				}
				else
				{
					throw new TypeNotPresentException(token, null);
				}
			}
		}
		_value = temp;
	}

	public MessageLevelSecurityRequirements computeUnion(MessageLevelSecurityRequirements other)
	{
		if (other == null)
		{
			return new MessageLevelSecurityRequirements(_value);
		}
		return new MessageLevelSecurityRequirements(_value | other._value);
	}

	public boolean equals(Object other)
	{
		return (((MessageLevelSecurityRequirements) other)._value == _value);
	}

	public boolean superset(MessageLevelSecurityRequirements other)
	{
		if ((other._value & _value) == other._value)
		{
			return true;
		}

		return false;
	}

	public boolean isNone()
	{
		return ((_value == NONE) || (_value == WARN));
	}

	public boolean isSign()
	{
		return ((_value & SIGN) > 0);
	}

	public boolean isEncrypt()
	{
		return ((_value & ENCRYPT) > 0);
	}

	public boolean isWarn()
	{
		return ((_value & WARN) > 0);
	}

	public String toString()
	{
		if (_value == NONE)
		{
			return "NONE";
		}

		String retval = "";
		if ((_value & SIGN) > 0)
		{
			retval += "|SIGN";
		}

		if ((_value & ENCRYPT) > 0)
		{
			retval += "|ENCRYPT";
		}

		if ((_value & WARN) > 0)
		{
			retval += "|WARN";
		}

		return retval.substring(1);
	}

}
