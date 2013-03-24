package edu.virginia.vcgr.genii.client.dialog.validators;

import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.dialog.InputValidator;

public class ContainsTextValidator extends RegularExpressionValidator
{
	private ContainsTextValidator(String text, boolean mustContain, String message)
	{
		super(Pattern.compile("^.*\\Q" + text + "\\E.*$"), mustContain, message);
	}

	static public InputValidator mustContainText(String text, String message)
	{
		return new ContainsTextValidator(text, true, message);
	}

	static public InputValidator mustNotContainText(String text, String message)
	{
		return new ContainsTextValidator(text, false, message);
	}
}