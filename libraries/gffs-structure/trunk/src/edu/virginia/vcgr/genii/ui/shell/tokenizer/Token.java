package edu.virginia.vcgr.genii.ui.shell.tokenizer;

public class Token
{
	private boolean _isSpace;
	private String _token;

	private Token(String token, boolean isSpace)
	{
		_token = token;
		_isSpace = isSpace;
	}

	final public boolean isSpaceToken()
	{
		return _isSpace;
	}

	@Override
	final public String toString()
	{
		return String.format("%s%s", _isSpace ? "s|" : "", _token);
	}

	final public String token()
	{
		return _token;
	}

	static Token createSpaceToken(String token)
	{
		return new Token(token, true);
	}

	static Token createWordToken(String token)
	{
		return new Token(token, false);
	}
}