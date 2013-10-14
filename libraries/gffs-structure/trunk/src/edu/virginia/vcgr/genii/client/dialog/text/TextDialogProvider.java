package edu.virginia.vcgr.genii.client.dialog.text;

import java.io.BufferedReader;
import java.io.PrintWriter;

import edu.virginia.vcgr.genii.client.dialog.CheckBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.CheckBoxItem;
import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.YesNoDialog;
import edu.virginia.vcgr.genii.client.dialog.YesNoSelection;

public class TextDialogProvider implements DialogProvider
{
	private ConsolePackage _package;

	public TextDialogProvider(PrintWriter stdout, PrintWriter stderr, BufferedReader stdin)
	{
		_package = new ConsolePackage(stdout, stderr, stdin);
	}

	@Override
	public InformationDialog createErrorDialog(String title, TextContent errorContent) throws DialogException,
		UserCancelException
	{
		return new TextInformationDialog(title, _package, errorContent, true);
	}

	@Override
	public InputDialog createHiddenInputDialog(String title, String prompt) throws DialogException, UserCancelException
	{
		TextInputDialog dialog = (TextInputDialog) createInputDialog(title, prompt);
		dialog.setHidden(true);

		return dialog;
	}

	@Override
	public InformationDialog createInformationDialog(String title, TextContent informationContent) throws DialogException,
		UserCancelException
	{
		return new TextInformationDialog(title, _package, informationContent, false);
	}

	@Override
	public InputDialog createInputDialog(String title, String prompt) throws DialogException, UserCancelException
	{
		return new TextInputDialog(title, _package, prompt);
	}

	@Override
	public ComboBoxDialog createComboBoxDialog(String title, String prompt, MenuItem defaultItem, MenuItem... items)
		throws DialogException, UserCancelException
	{
		return new TextComboBoxDialog(title, _package, prompt, defaultItem, items);
	}

	@Override
	public ComboBoxDialog createSingleListSelectionDialog(String title, String prompt, MenuItem defaultItem, MenuItem... items)
		throws DialogException, UserCancelException
	{
		return new TextComboBoxDialog(title, _package, prompt, defaultItem, items);
	}

	@Override
	public CheckBoxDialog createCheckBoxDialog(String title, String prompt, CheckBoxItem... items) throws DialogException,
		UserCancelException
	{
		return new TextCheckBoxDialog(title, _package, prompt, items);
	}

	@Override
	public YesNoDialog createYesNoDialog(String title, String prompt, YesNoSelection defaultAnswer) throws DialogException,
		UserCancelException
	{
		return new TextYesNoDialog(title, _package, prompt, defaultAnswer);
	}
}