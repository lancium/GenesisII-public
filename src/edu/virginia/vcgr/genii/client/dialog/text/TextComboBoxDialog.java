package edu.virginia.vcgr.genii.client.dialog.text;

import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;

public class TextComboBoxDialog extends TextInputDialog implements
		ComboBoxDialog
{
	private MenuItem []_items;
	private MenuItem _selectedItem;
	private Map<String, MenuItem> _itemMap;
	private int _longestTag;
	
	public TextComboBoxDialog(String title, ConsolePackage pkg,
		String prompt, MenuItem defaultItem, MenuItem...items)
			throws DialogException
	{
		super(title, pkg, prompt);
	
		_selectedItem  = null;
		_items = items;
		_itemMap = new HashMap<String, MenuItem>();
	
		_longestTag = 0;
		for (MenuItem item : _items)	
		{
			String tag = item.getTag();
			
			if (_itemMap.containsKey(tag))
				throw new DialogException(
					"More then one menu item has the tag \"" + tag + "\".");
			_itemMap.put(tag, item);
			_longestTag = Math.max(_longestTag, tag.length());
		}
	
		if (defaultItem != null)
			setDefaultAnswer(defaultItem.getTag());
		
		setInputValidator(new InternalInputValidator());
	}
	
	@Override
	public MenuItem getSelectedItem()
	{
		return _selectedItem;
	}

	@Override
	protected void showContent()
	{
		String pattern = String.format("\t%%%ds\t%%s\n", (_longestTag + 2));
		
		_package.stdout().println();
		for (MenuItem item : _items)
		{
			_package.stdout().format(pattern, String.format("[%s]", item.getTag()), item);
		}
		_package.stdout().println();
		
		super.showContent();
	}
	
	private class InternalInputValidator implements InputValidator
	{
		@Override
		public String validateInput(String input)
		{
			_selectedItem = _itemMap.get(input);
			if (_selectedItem != null)
				return null;
			
			return "Please select an option from the menu.";
		}	
	}
}