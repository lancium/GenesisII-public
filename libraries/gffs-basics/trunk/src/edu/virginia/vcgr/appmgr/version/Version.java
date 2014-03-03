package edu.virginia.vcgr.appmgr.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version>
{
	static final private Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\s+Build\\s+(\\d+)$");

	private int _majorVersion;
	private int _minorVersion;
	private int _subMinorVersion;
	private int _buildNumber;

	public Version(int major, int minor, int subminor, int buildNumber)
	{
		_majorVersion = major;
		_minorVersion = minor;
		_subMinorVersion = subminor;
		_buildNumber = buildNumber;
	}

	public Version(String str)
	{
		Matcher matcher = VERSION_PATTERN.matcher(str);
		if (!matcher.matches())
			throw new IllegalArgumentException("Version string wasn't of the form ###.###.### Build ####");

		_majorVersion = Integer.parseInt(matcher.group(1));
		_minorVersion = Integer.parseInt(matcher.group(2));
		_subMinorVersion = Integer.parseInt(matcher.group(3));
		_buildNumber = Integer.parseInt(matcher.group(4));
	}

	public boolean equals(Version other)
	{
		return compareTo(other) == 0;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Version)
			return equals((Version) other);

		return false;
	}

	@Override
	public int hashCode()
	{
		return (_majorVersion << 3) ^ (_minorVersion << 2) ^ (_subMinorVersion << 1) ^ _buildNumber;
	}

	@Override
	public String toString()
	{
		return String.format("%d.%d.%d Build %d", _majorVersion, _minorVersion, _subMinorVersion, _buildNumber);
	}

	@Override
	public int compareTo(Version o)
	{
		int diff = _majorVersion - o._majorVersion;
		if (diff != 0)
			return diff;
		diff = _minorVersion - o._minorVersion;
		if (diff != 0)
			return diff;
		diff = _subMinorVersion - o._subMinorVersion;
		if (diff != 0)
			return diff;
		return _buildNumber - o._buildNumber;
	}

	static public Version EMPTY_VERSION = new Version(0, 0, 0, 0);
}