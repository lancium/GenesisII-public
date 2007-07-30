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
  * Keeps track of version numbers.
  *
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class Version implements Serializable, Comparable<Version>
{
	static private Pattern _stringFormat = Pattern.compile(
		"^([0-9]+)\\.([0-9]+)\\.([0-9]+)$");
	
	static final long serialVersionUID = 0;
	
	private int _major;
	private int _minor;
	private int _subminor;
	
	public Version(String version)
	{
		Matcher m = _stringFormat.matcher(version);
		if (!m.matches())
			throw new IllegalArgumentException("Version string \"" +
				version + "\" is invalid.");
		
		_major = Integer.parseInt(m.group(1));
		_minor = Integer.parseInt(m.group(2));
		_subminor = Integer.parseInt(m.group(3));
	}
	
	public Version(int major)
	{
		this(major, 0, 0);
	}
	
	public Version(int major, int minor)
	{
		this(major, minor, 0);
	}
	
	public Version(int major, int minor, int subminor)
	{
		_major = major;
		_minor = minor;
		_subminor = subminor;
	}
	
	public boolean equals(Version other)
	{
		return (_major == other._major) && (_minor == other._minor) &&
			(_subminor == other._subminor);
	}
	
	public boolean equals(Object other)
	{
		return equals((Version)other);
	}
	
	public int hashCode()
	{
		return (_major << 3) ^ (_minor << 1) ^ (_subminor);
	}
	
	public int compareTo(Version other)
	{
		if (_major != other._major)
			return _major - other._major;
		if (_minor != other._minor)
			return _minor - other._minor;
		return _subminor - other._subminor;
	}
	
	public String toString()
	{
		return String.format("%d.%d.%d", _major, _minor, _subminor);
	}
	
	public boolean sameMajor(Version other)
	{
		return _major == other._major;
	}
	
	public boolean sameMinor(Version other)
	{
		return (_major == other._major) && (_minor == other._minor);
	}
	
	public Version nextSubMinor()
	{
		return new Version(_major, _minor, _subminor + 1);
	}
	
	public Version nextMinor()
	{
		return new Version(_major, _minor + 1);
	}
	
	public Version nextMajor()
	{
		return new Version(_major + 1);
	}
}
