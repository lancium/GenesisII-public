package edu.virginia.vcgr.genii.client.nativeq.sge;

import java.util.List;

import edu.virginia.vcgr.genii.client.nativeq.AbstractJobToken;

public class SGEJobToken extends AbstractJobToken
{
	static final long serialVersionUID = 0L;

	private String _token;

	public SGEJobToken(String token)
	{
		if (token == null)
			throw new IllegalArgumentException("Token parameter cannot be null.");

		_token = token;
	}

	public SGEJobToken(String token, List<String> cmdLine)
	{
		if (token == null)
			throw new IllegalArgumentException("Token parameter cannot be null.");

		_token = token;
		_cmdLine = cmdLine;
	}

	public String toString()
	{
		return _token;
	}

	public boolean equals(SGEJobToken other)
	{
		return _token.equals(other._token);
	}

	public boolean equals(Object other)
	{
		if (other instanceof SGEJobToken)
			return equals((SGEJobToken) other);

		return false;
	}

	public int hashCode()
	{
		return _token.hashCode();
	}
}