package edu.virginia.vcgr.genii.client.nativeq.slurm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.nativeq.AbstractJobToken;

public class SLURMJobToken extends AbstractJobToken
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(SLURMJobToken.class);

	private String _token;

	public static String stripOutNumericalToken(String toStrip)
	{
		Pattern numStripper = Pattern.compile("^[^0-9]*([0-9]+)[^0-9]*$");
		Matcher matcher = numStripper.matcher(toStrip);
		if (!matcher.matches()) {
			_logger.warn("unexpected non-match to find slurm job ID in string: " + toStrip);
			// hope that they know what to do with this, since we can't get a number out of it.
			return toStrip;
		}

		if (_logger.isTraceEnabled())
			_logger.debug("calculated job token for slurm as: " + matcher.group(1));

		return matcher.group(1);
	}

	public SLURMJobToken(String token)
	{
		if (token == null)
			throw new IllegalArgumentException("Token parameter cannot be null.");

		_token = stripOutNumericalToken(token);
	}

	public SLURMJobToken(String token, List<String> cmdLine)
	{
		if (token == null)
			throw new IllegalArgumentException("Token parameter cannot be null.");

		_token = stripOutNumericalToken(token);
		_cmdLine = cmdLine;
	}

	public String toString()
	{
		return _token;
	}

	public boolean equals(SLURMJobToken other)
	{
		return _token.equals(other._token);
	}

	public boolean equals(Object other)
	{
		if (other instanceof SLURMJobToken)
			return equals((SLURMJobToken) other);

		return false;
	}

	public int hashCode()
	{
		return _token.hashCode();
	}

	static public void main(String[] args)
	{
		// typical slurm output on submission.
		String fud = "Submitted batch job 23145";
		String justNum = stripOutNumericalToken(fud);
		System.out.println("input string was: " + fud);
		System.out.println("got just a batch identifier of '" + justNum + "'; is that right?");

		fud = "Submitted batch job real funky like 727271 and you had better find it.";
		justNum = stripOutNumericalToken(fud);
		System.out.println("input string was: " + fud);
		System.out.println("got just a batch identifier of '" + justNum + "'; is that right?");

	}
}
