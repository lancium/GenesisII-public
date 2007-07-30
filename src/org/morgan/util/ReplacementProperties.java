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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class ReplacementProperties extends Properties
{
	static private Pattern _VAR_PATTERN = Pattern.compile(
	"\\$\\{([^}]*)}");

	static final long serialVersionUID = 0;
	
	private Properties _firstOverride = null;
	private Properties _secondOverride = null;
	
	public ReplacementProperties()
	{
		this(null, null);
	}
	
	public ReplacementProperties(Properties firstOverride)
	{
		this(firstOverride, null);
	}
	
	public ReplacementProperties(Properties firstOverride,
		Properties secondOverride)
	{
		_firstOverride = firstOverride;
		_secondOverride = secondOverride;
	}
	
	public String getProperty(String propertyName)
	{
		return getProperty(propertyName, null);
	}
	
	public String getProperty(String propertyName, String def)
	{
		String result = null;
		
		result = super.getProperty(propertyName);
		if ((result == null) && (_firstOverride != null) )
		{
			result = _firstOverride.getProperty(propertyName);
			if ( (result == null) && (_secondOverride != null) )
			{
				result = _secondOverride.getProperty(propertyName);
			}
		}
		if (result == null)
			result = def;
		
		if (result == null)
			return null;
		
		while (true)
		{
			Matcher m = _VAR_PATTERN.matcher(result);
			if (!m.find())
				break;
			
			String value = getProperty(m.group(1));
			if (value == null)
				value = "";
			result = result.substring(0, m.start()) +
				value + result.substring(m.end());
		}
		
		return result;
	}
}
