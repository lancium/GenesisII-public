package edu.virginia.vcgr.appmgr.patch;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.appmgr.net.Hostname;

public class HostRestriction
{
	private String _value;
	private boolean _isHostname;
	private boolean _isRegularExpression;

	private HostRestriction(String value, boolean isHostname, boolean isRegularExpression)
	{
		_value = value;
		_isHostname = isHostname;
		_isRegularExpression = isRegularExpression;
	}

	public void emit(PrintStream out)
	{
		out.format("\t\t\t<host use-ip=\"%s\">\n", !_isHostname);
		out.format("\t\t\t\t<%1$s>%2$s</%1$s>\n", _isRegularExpression ? "pattern" : "hostname", _value);
		out.println("\t\t\t</host>");
	}

	public String getValue()
	{
		return _value;
	}

	public boolean isHostname()
	{
		return _isHostname;
	}

	public boolean isRegularExpression()
	{
		return _isRegularExpression;
	}

	public boolean satisfies()
	{
		Pattern pattern =
			Pattern.compile(_isRegularExpression ? _value : String.format("^%s$", Pattern.quote(_value)),
				Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(_isHostname ? Hostname.getCurrentHostname() : Hostname.getCurrentIPAddress());
		return matcher.matches();
	}

	static public HostRestriction restrictToHostname(String hostname)
	{
		return new HostRestriction(hostname, true, false);
	}

	static public HostRestriction restrictToHostnamePattern(String pattern)
	{
		return new HostRestriction(pattern, true, true);
	}

	static public HostRestriction restrictToIPAddress(String ip)
	{
		return new HostRestriction(ip, false, false);
	}

	static public HostRestriction restrictToIPAddressPattern(String pattern)
	{
		return new HostRestriction(pattern, false, true);
	}
}