package edu.virginia.vcgr.genii.client.dialog.validators;

import edu.virginia.vcgr.genii.client.dialog.InputValidator;

public class NonEmptyValidator implements InputValidator
{
	private String _emptyMessage;

	public NonEmptyValidator(String emptyMessage)
	{
		_emptyMessage = emptyMessage;
	}

	@Override
	public String validateInput(String input)
	{
		if (input == null || input.isEmpty())
			return _emptyMessage;

		return null;
	}
}