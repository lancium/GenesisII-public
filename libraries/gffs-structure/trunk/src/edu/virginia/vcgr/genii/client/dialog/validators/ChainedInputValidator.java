package edu.virginia.vcgr.genii.client.dialog.validators;

import edu.virginia.vcgr.genii.client.dialog.InputValidator;

public class ChainedInputValidator implements InputValidator
{
	private InputValidator[] _validators;

	public ChainedInputValidator(InputValidator... validators)
	{
		_validators = validators;
	}

	@Override
	public String validateInput(String input)
	{
		for (InputValidator validator : _validators) {
			String msg = validator.validateInput(input);
			if (msg != null)
				return msg;
		}

		return null;
	}
}
