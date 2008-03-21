package edu.virginia.vcgr.genii.client.utils.dialog.text;

import java.util.HashSet;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuChoice;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuWidget;

/**
 * A text based menu widget.
 *  
 * @author mmm2a
 */
public class TextMenuWidget extends AbstractTextWidget implements MenuWidget
{
	private String _prompt;
	private Vector<MenuChoice> _choices = new Vector<MenuChoice>();
	private Object _selectedChoice;
	private String _formatString;
	
	/**
	 * Construct a new text based menu widget.
	 * 
	 * @param provider The text provider for this widget.
	 * @param title The initial title.
	 */
	public TextMenuWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
		
		_selectedChoice = null;
		setPrompt("Selection?");
	}
	
	@Override
	public void setPrompt(String prompt)
	{
		if (prompt == null)
			throw new IllegalArgumentException("Prompt cannot be null.");
		
		_prompt = prompt;
	}
	
	@Override
	public Object getSelectedChoice()
	{
		return _selectedChoice;
	}

	@Override
	public void setChoices(MenuChoice... choices)
	{
		if (choices == null)
			throw new IllegalArgumentException("Choices cannot be null.");

		_choices.clear();
		HashSet<String> knownKeys = new HashSet<String>(choices.length);
		
		int longestKey = 0;
		for (MenuChoice choice : choices)
		{
			String key = choice.getKey();
			if (knownKeys.contains(key))
				throw new IllegalArgumentException("The key \"" + key 
					+ "\" appears multiple times in the menu.");
			knownKeys.add(key);
			
			if (key.length() > longestKey)
				longestKey = key.length();
			
			_choices.add(choice);
		}
		
		_formatString = "\t[%-" + longestKey + "s]\t%s\n";
	}

	@Override
	public void setChoices(Object... choices)
	{
		if (choices == null)
			throw new IllegalArgumentException("Choices cannot be null.");

		MenuChoice []mChoices = new MenuChoice[choices.length];
		
		for (int lcv = 0; lcv < choices.length; lcv++)
		{
			mChoices[lcv] = new MenuChoice(Integer.toString(lcv), choices[lcv]);
		}
		
		setChoices(mChoices);
	}

	@Override
	public void showWidget() throws DialogException
	{
		if (_choices.size() == 0)
			throw new DialogException(
				"No choices have been set for this menu.");
		
		_selectedChoice = null;
		
		TextWidgetProvider twp = TextWidgetProvider.class.cast(getProvider());
		
		String detailedHelp = getDetailedHelp();
		
		while (true)
		{
			if (detailedHelp != null)
			{
				twp.stdout.println(detailedHelp);
				twp.stdout.println();
			}
		
			for (MenuChoice choice : _choices)
			{
				twp.stdout.format(_formatString, choice.getKey(), choice.getValue());
			}
			
			twp.stdout.println();
			twp.stdout.print(_prompt + "  ");
			twp.stdout.flush();
			
			String line = twp.readline();
			if (line.length() == 0)
				showErrorMessage("Please select a menu choice.");
			else
			{
				for (MenuChoice choice : _choices)
				{
					if (line.equals(choice.getKey()))
					{
						_selectedChoice = choice.getValue();
						return;
					}
				}
				
				showErrorMessage("Choice \"" + line + "\" is not recognized.");
			}
		}
	}
}