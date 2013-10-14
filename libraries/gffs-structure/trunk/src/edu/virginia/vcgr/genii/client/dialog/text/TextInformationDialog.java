package edu.virginia.vcgr.genii.client.dialog.text;

import java.io.PrintWriter;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.TextContent;

public class TextInformationDialog extends TextInputDialog implements InformationDialog
{
	static final private String PROMPT = "Please hit <enter> to continue:";

	private TextContent _content;
	private boolean _isError;

	public TextInformationDialog(String title, ConsolePackage pkg, TextContent content, boolean isError)
	{
		super(title, pkg, PROMPT);

		_content = content;
		_isError = isError;

		setInputValidator(new InternalInputValidator());
	}

	@Override
	protected void showContent()
	{
		PrintWriter stream = (_isError ? _package.stderr() : _package.stdout());

		stream.println();
		stream.println(_content);
		stream.println();
		StreamUtils.close(stream);
		super.showContent();
	}

	static private class InternalInputValidator implements InputValidator
	{
		@Override
		public String validateInput(String input)
		{
			if (input.length() == 0)
				return null;

			return String.format("\"%s\" is not a valid response!", input);
		}
	}
}