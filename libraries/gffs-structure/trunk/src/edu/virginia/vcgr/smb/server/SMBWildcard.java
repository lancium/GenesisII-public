package edu.virginia.vcgr.smb.server;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

public class SMBWildcard implements RNSFilter
{
	private String winPattern;

	public SMBWildcard(String winPattern)
	{
		// Pattern matching is done by filename component.
		// Eg. match ????.????.???? with foo.bar.txt -> match [????, ????, ????] with [foo, bar, txt], which succeeds
		// Unfortunately the kleene star makes this much more difficult as it can span components
		// Eg. match *.???? with foo.bar.txt -> match [*, *, ????] with [foo, bar, txt]
		// More complex cases
		// Eg. match ????.?.....
		// ? matches any character except dot; it can match nothing if everything before the next dot has been matched
		// . matches a period or EOS
		// * is like .*

		this.winPattern = winPattern.replace('>', '?').replace('"', '.').replace('<', '*');
	}

	public static boolean matches(String pattern, String with, int pIdx, int wIdx)
	{
		if (pattern.length() == pIdx)
			return with.length() == wIdx;

		char pChar = pattern.charAt(pIdx);

		if (pChar == '*') {
			if (with.length() == wIdx)
				return matches(pattern, with, pIdx + 1, wIdx);

			// Try to be greedy first
			if (matches(pattern, with, pIdx, wIdx + 1)) {
				return true;
			} else {
				return matches(pattern, with, pIdx + 1, wIdx);
			}
		} else if (pChar == '.') {
			if (with.length() == wIdx)
				return matches(pattern, with, pIdx + 1, wIdx);

			char wChar = with.charAt(wIdx);
			return wChar == '.' && matches(pattern, with, pIdx + 1, wIdx + 1);
		} else if (pChar == '?') {
			if (with.length() == wIdx)
				return matches(pattern, with, pIdx + 1, wIdx);

			char wChar = with.charAt(wIdx);
			if (wChar == '.')
				return matches(pattern, with, pIdx + 1, wIdx);

			return matches(pattern, with, pIdx + 1, wIdx + 1);
		} else {
			if (with.length() == wIdx)
				return false;

			return Character.toLowerCase(pChar) == Character.toLowerCase(with.charAt(wIdx)) && matches(pattern, with, pIdx + 1, wIdx + 1);
		}
	}

	public boolean matches(String with)
	{
		return SMBWildcard.matches(this.winPattern, with, 0, 0);
	}

	@Override
	public boolean matches(RNSPath testEntry)
	{
		return matches(testEntry.getName());
	}
}
