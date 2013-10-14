package edu.virginia.vcgr.genii.client.appdesc;

import java.io.Serializable;
import java.util.regex.Pattern;

public class ApplicationVersion implements Serializable, Comparable<ApplicationVersion>
{
	static final long serialVersionUID = 0L;

	static private Pattern _VERSION_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");

	private int[] _version;

	public ApplicationVersion(String versionString)
	{
		if (!_VERSION_PATTERN.matcher(versionString).matches())
			throw new IllegalArgumentException("The version given \"" + versionString + "\" is not a valid version string.");

		String[] matches = versionString.split("\\.");
		_version = new int[matches.length];
		for (int lcv = 0; lcv < matches.length; lcv++)
			_version[lcv] = Integer.parseInt(matches[lcv]);
	}

	public int compareTo(ApplicationVersion o)
	{
		int myVersionLength = _version.length;
		int otherLength = o._version.length;
		int maxLength = (myVersionLength > otherLength) ? myVersionLength : otherLength;
		int lcv = 0;
		int myVal;
		int otherVal;

		while (true) {
			if (lcv >= myVersionLength)
				myVal = 0;
			else
				myVal = _version[lcv];

			if (lcv >= otherLength)
				otherVal = 0;
			else
				otherVal = _version[lcv];

			int diff = myVal - otherVal;
			if (diff != 0)
				return diff;

			if (lcv >= maxLength)
				return 0;

			lcv++;
		}
	}

	public int hashCode()
	{
		int val = 0xDEADBEEF;
		for (int lcv = 0; lcv < _version.length; lcv++) {
			val <<= 3;
			val ^= _version[lcv];
		}

		return val;
	}

	public boolean equals(ApplicationVersion other)
	{
		return compareTo(other) == 0;
	}

	public boolean equals(Object other)
	{
		return equals((ApplicationVersion) other);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer(_version.length * 3);

		for (int lcv = 0; lcv < _version.length; lcv++) {
			if (lcv != 0)
				buffer.append('.');
			buffer.append(_version[lcv]);
		}

		return buffer.toString();
	}
}