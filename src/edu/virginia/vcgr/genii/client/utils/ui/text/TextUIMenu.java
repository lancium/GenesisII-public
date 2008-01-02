package edu.virginia.vcgr.genii.client.utils.ui.text;

import edu.virginia.vcgr.genii.client.utils.ui.AbstractMenu;
import edu.virginia.vcgr.genii.client.utils.ui.UIException;
import edu.virginia.vcgr.genii.client.utils.ui.UIProvider;

/**
 * Implements the Menu widget by printing to stdout and reading from
 * stdin.
 * 
 * @author mmm2a
 */
public class TextUIMenu extends AbstractMenu
{
	private String _itemFormat;
	
	/**
	 * Construct a new Text-based Menu widget.
	 * 
	 * @param provider The UI provider creating this menu.
	 * @param header A header (if any) to display before
	 * menu options.  This value can be null.
	 * @param footer A footer to display after menu options.  This
	 * is essentially the prompt for the menu and cannot be null.
	 * @param includeCancel Whether or not the menu class should
	 * automatically include a cancel menu option.
	 * @param elements The elements to include in the menu.
	 * 
	 * @throws UIException
	 */
	public TextUIMenu(
		TextUIProvider provider, String header, String footer,
		boolean includeCancel, UIProvider.MenuElement []elements)
		throws UIException
	{
		super(provider, header, footer, includeCancel, elements);
		
		/* We have to iterate through the menu items finding the largest
		 * key that is included.  This will help us determine the proper
		 * formatting to use so that things line up well on the output
		 * device.
		 */
		int maxKeyWidth = 0;
		for (UIProvider.MenuElement element : _elements)
		{
			String key = element.getKey();
			if (key.length() > maxKeyWidth)
				maxKeyWidth = key.length();
		}
		
		/* We store the format string so that we don't have to reform it
		 * every time we print a new menu item out.
		 */
		_itemFormat = "\t[%-" + maxKeyWidth + "s]\t%s";
	}
	
	@Override
	protected String internalChoose() throws UIException
	{
		TextUIProvider provider = (TextUIProvider)_provider;
		
		if (_header != null)
		{
			provider.getStdout().println(_header);
			provider.getStdout().println();
		}
		
		for (UIProvider.MenuElement element : _elements)
		{
			provider.getStdout().format(_itemFormat, element.getKey(), element.getValue());
		}
		provider.getStdout().print(_footer + "  ");
		provider.getStdout().flush();
		
		return provider.readTrimmedLine();
	}

	@Override
	public void displayError(String message) throws UIException
	{
		((TextUIProvider)_provider).displayError(message);
	}
}