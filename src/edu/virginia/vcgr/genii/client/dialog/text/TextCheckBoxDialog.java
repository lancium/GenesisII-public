package edu.virginia.vcgr.genii.client.dialog.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.virginia.vcgr.genii.client.dialog.CheckBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.CheckBoxItem;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public class TextCheckBoxDialog extends TextInputDialog implements CheckBoxDialog
{
	static final long serialVersionUID = 0L;

	private CheckBoxItem[] _items;
	private Map<String, CheckBoxItem> _itemMap;
	private int _longestTag;

	public TextCheckBoxDialog(String title, ConsolePackage pkg, String prompt, CheckBoxItem... items) throws DialogException
	{
		super(title, pkg, prompt);

		_items = items;

		_itemMap = new HashMap<String, CheckBoxItem>();

		_longestTag = 0;
		for (CheckBoxItem item : _items) {
			String tag = item.getTag();

			if (_itemMap.containsKey(tag))
				throw new DialogException("More then one menu item has the tag \"" + tag + "\".");
			_itemMap.put(tag, item);
			_longestTag = Math.max(_longestTag, tag.length());
		}

		setInputValidator(new InternalInputValidator());
	}

	@Override
	protected String generateHint()
	{
		if (getHelp() != null) {
			return "Hint:  You may enter \"Cancel\" to cancel this selection, "
				+ "or \"Help\" to get help.\nPlease hit <enter> when done!";
		} else {
			return "Hint:  You may enter \"Cancel\" to cancel this selection.\n" + "Please hit <enter> when done!";
		}
	}

	@Override
	protected void showContent()
	{
		String pattern = String.format("\t(%%s) %%%ds\t%%s%%s\n", (_longestTag + 2));

		_package.stdout().println();
		for (CheckBoxItem item : _items) {
			_package.stdout().format(pattern, item.isChecked() ? "*" : " ", String.format("[%s]", item.getTag()), item,
				item.isEditable() ? "" : " <not modifiable>");
		}
		_package.stdout().println();

		super.showContent();
	}

	private class InternalInputValidator implements InputValidator
	{
		@Override
		public String validateInput(String input)
		{
			CheckBoxItem item;
			item = _itemMap.get(input);
			if (item != null) {
				if (item.isEditable()) {
					item.setChecked(!item.isChecked());
					return null;
				}

				return "The item \"" + item.getTag() + "\" is not editable.";
			}

			return "Please select an option from the menu.";
		}
	}

	@Override
	public void showDialog() throws UserCancelException, DialogException
	{
		String answer;

		while (true) {
			answer = null;

			showContent();

			answer = _package.readLine();
			if (answer.length() == 0)
				return;

			if (answer.equalsIgnoreCase("Cancel"))
				throw new UserCancelException();
			else if (getHelp() != null && answer.equalsIgnoreCase("Help")) {
				_package.stdout().println();
				_package.stdout().println(getHelp());
				_package.stdout().println();
				continue;
			}

			if (_validator != null) {
				String msg = _validator.validateInput(answer);
				if (msg != null) {
					_package.stderr().println(msg);
					_package.stderr().println();
					continue;
				}
			}
		}
	}

	@Override
	public Collection<MenuItem> getCheckedItems()
	{
		Collection<MenuItem> items = new LinkedList<MenuItem>();

		for (CheckBoxItem item : _items) {
			if (item.isChecked())
				items.add(item);
		}

		return items;
	}
}