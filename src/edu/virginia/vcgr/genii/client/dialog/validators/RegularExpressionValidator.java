package edu.virginia.vcgr.genii.client.dialog.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.dialog.InputValidator;

public class RegularExpressionValidator implements InputValidator
{
	private Pattern _expression;
	private boolean _mustMatch;
	private String _message;

	protected RegularExpressionValidator(Pattern expression, boolean mustMatch, String message)
	{
		_expression = expression;
		_mustMatch = mustMatch;
		_message = message;
	}

	@Override
	public String validateInput(String input)
	{
		Matcher matcher = _expression.matcher(input);
		if (matcher.matches())
			return _mustMatch ? null : _message;
		else
			return _mustMatch ? _message : null;
	}

	static public InputValidator mustMatchValidator(Pattern expression, String message)
	{
		return new RegularExpressionValidator(expression, true, message);
	}

	static public InputValidator mustMatchValidator(String expression, String message)
	{
		return mustMatchValidator(Pattern.compile(expression), message);
	}

	static public InputValidator mustNotMatchValidator(Pattern expression, String message)
	{
		return new RegularExpressionValidator(expression, false, message);
	}

	static public InputValidator mustNotMatchValidator(String expression, String message)
	{
		return mustNotMatchValidator(Pattern.compile(expression), message);
	}
}