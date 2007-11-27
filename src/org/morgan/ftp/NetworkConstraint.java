package org.morgan.ftp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkConstraint
{
	static private Pattern _CONSTRAINT_PATTERN =
		Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
	private int []_constraint;
	
	public NetworkConstraint(String constraint)
	{
		Matcher matcher = _CONSTRAINT_PATTERN.matcher(constraint);
		if (!matcher.matches())
			throw new IllegalArgumentException(
				"Constraint must be of form \"###.###.###.###\".");
		
		_constraint = new int[] {
			Integer.parseInt(matcher.group(1)),
			Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)),
			Integer.parseInt(matcher.group(4))
		};
	}
	
	public boolean matches(SocketAddress addr)
	{
		if (!(addr instanceof InetSocketAddress))
			return false;
		
		InetAddress iaddr = ((InetSocketAddress)addr).getAddress();
		String sAddr = iaddr.getHostAddress();
		
		Matcher matcher = _CONSTRAINT_PATTERN.matcher(sAddr);
		if (!matcher.matches())
			return false;
		
		int []ip = new int[] {
			Integer.parseInt(matcher.group(1)),
			Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)),
			Integer.parseInt(matcher.group(4))
		};
		
		if (ip.length != _constraint.length)
			return false;
		
		for (int lcv = 0; lcv < ip.length; lcv++)
		{
			if (_constraint[lcv] == 0)
				continue;
			if (_constraint[lcv] != ip[lcv])
				return false;
		}
		
		return true;
	}
}