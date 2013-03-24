package edu.virginia.vcgr.genii.client.dialog.text;

import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public class TextInputDialog extends AbstractTextDialog implements InputDialog
{
	private String _defaultAnswer;
	protected InputValidator _validator;
	private String _hint;
	private String _prompt;
	private boolean _hidden;
	private String _answer;

	protected String generateHint()
	{
		if (getHelp() != null) {
			return "Hint:  You may enter \"Cancel\" to cancel this selection, or \"Help\" to get help.";
		} else {
			return "Hint:  You may enter \"Cancel\" to cancel this selection.";
		}
	}

	TextInputDialog(String title, ConsolePackage pkg, String prompt)
	{
		super(title, pkg);

		_defaultAnswer = null;
		_answer = null;
		_prompt = prompt;

		_hint = generateHint();
	}

	protected void setHidden(boolean isHidden)
	{
		_hidden = isHidden;
	}

	@Override
	public void setHelp(TextContent helpContent)
	{
		super.setHelp(helpContent);

		generateHint();
	}

	@Override
	public void setDefaultAnswer(String defaultAnswer)
	{
		_defaultAnswer = defaultAnswer;
	}

	@Override
	public String getDefaultAnswer()
	{
		return _defaultAnswer;
	}

	@Override
	public InputValidator getInputValidator()
	{
		return _validator;
	}

	@Override
	public void setInputValidator(InputValidator validator)
	{
		_validator = validator;
	}

	protected void showContent()
	{
		_package.stdout().println(_hint);
		_package.stdout().print(_prompt);
		if (_defaultAnswer != null)
			_package.stdout().format(" [%s]", _defaultAnswer);
		_package.stdout().print("  ");
	}

	@Override
	public void showDialog() throws DialogException, UserCancelException
	{
		while (true) {
			_answer = null;

			showContent();

			_answer = _package.readLine(_hidden);
			if ((_answer.length() == 0) && (_defaultAnswer != null))
				_answer = _defaultAnswer;

			if (_answer.equalsIgnoreCase("Cancel"))
				throw new UserCancelException();
			else if (getHelp() != null && _answer.equalsIgnoreCase("Help")) {
				_package.stdout().println();
				_package.stdout().println(getHelp());
				_package.stdout().println();
				continue;
			}

			if (_validator != null) {
				String msg = _validator.validateInput(_answer);
				if (msg != null) {
					_package.stderr().println(msg);
					_package.stderr().println();
					continue;
				}
			}

			return;
		}
	}

	@Override
	public String getAnswer()
	{
		return _answer;
	}
}