package edu.virginia.vcgr.genii.client.utils.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.virginia.vcgr.genii.client.utils.ui.UIProvider;

/**
 * This abstract class implements the common functionallity for a menu.
 * 
 * @author mmm2a
 */
public abstract class AbstractMenu extends AbstractUIElement implements UIMenu
{
	/**
	 * In case we need to auto-generate a cancel menu option, we have a list of
	 * suitable cancel keys for the menu.  If none of these work, a number will
	 * be used.
	 */
	static private final String []_CANCEL_KEYS = {"x", "X", "c", "C", "q", "Q"};
	
	protected String _header;
	protected String _footer;
	protected Collection<UIProvider.MenuElement> _elements;
	
	/**
	 * This internal class is simply used to mark the cancel menu option if we
	 * need one.  It is never seen outside the menu class.
	 * 
	 * @author mmm2a
	 */
	static private class CancelOption
	{
		public String toString()
		{
			return "Cancel";
		}
	}

	/**
	 * Given a list of known keys already in the menu, find a suitable key to
	 * use for the cancel option.
	 * 
	 * @param knownKeys A list of keys already in the menu.
	 * @return The new key to use for the cancel menu option.
	 */
	private static String findCancelKey(HashSet<String> knownKeys)
	{
		String cancelKey = null;
		
		/* First, see if any of the preferred cancel keys are available */
		for (String key : _CANCEL_KEYS)
		{
			if (!knownKeys.contains(key))
			{
				cancelKey = key;
				break;
			}
		}
		
		/* If all of the preferred cancel keys were already in use, then just
		 * find the lowest integer number that is available.
		 */
		if (cancelKey == null)
		{
			/* We can easily bound the number of integers to check because we
			 * know how many keys are already in the menu.
			 */
			for (int lcv = 0; lcv < knownKeys.size() + 1; lcv++)
			{
				String key = Integer.toString(lcv);
				if (!knownKeys.contains(key))
				{
					cancelKey = key;
					break;
				}
			}
		}
		
		return cancelKey;
	}
	
	/**
	 * Create a new abstract menu.
	 * 
	 * @param provider The UI provider responsible for this menu widget.
	 * @param header A header (if any) to display before menu options are
	 * indicated.  This value may be null.
	 * @param footer A footer (or prompt) to display after menu items.  This
	 * value cannot be null.
	 * @param includeCancel indicates whether or not a cancel option will be
	 * added automatically.
	 * @param elements The user supplied menu elements to include in this menu.
	 * 
	 * @throws UIException
	 */
	public AbstractMenu(
		UIProvider provider, String header, String footer,
		boolean includeCancel, UIProvider.MenuElement []elements)
			throws UIException
	{
		super(provider);
		
		if (footer == null)
			throw new IllegalArgumentException(
				"The footer argument for a menu cannot be null.");
		if (elements == null)
			throw new IllegalArgumentException(
				"The menu elements parameter for a menu cannot be null.");
		
		HashSet<String> knownKeys = new HashSet<String>();
		
		_header = header;
		_footer = footer;
		
		_elements = new ArrayList<UIProvider.MenuElement>(elements.length + 1);
		for (int lcv = 0; lcv < elements.length; lcv++)
		{
			UIProvider.MenuElement element = elements[lcv];
			if (element == null)
				throw new IllegalArgumentException(
					"Cannot have null elements inside of a menu elements list.");
			
			String key = element.getKey();
			
			/* Check to see if this menu key has already been added. */
			if (knownKeys.contains(key))
				throw new UIException("Key \"" + key + "\" already exists in menu.");
			
			/* Now keep track of the new element key so we can check 
			 * subsequent keys for uniqueness */
			knownKeys.add(key);
			
			/*
			 * Finally, add the element to the menu.
			 */
			_elements.add(element);
		}
		
		/* If the user wants cancel automatically included, we need to go ahead
		 * and generate that menu element (and key).
		 */
		if (includeCancel)
		{
			String cancelKey = findCancelKey(knownKeys);
			_elements.add(new UIProvider.MenuElement(cancelKey, new CancelOption()));
		}
	}
	
	@Override
	public Object choose() throws UIException
	{
		while (true)
		{
			String selection = internalChoose();
			if (selection == null || selection.length() == 0)
			{
				displayError("Please make a selection!");
				continue;
			}
			
			for (UIProvider.MenuElement element : _elements)
			{
				String key = element.getKey();
				if (key.equals(selection))
				{
					Object value = element.getValue();
					if (value instanceof CancelOption)
						return null;
					
					return value;
				}
			}
			
			displayError("Selection \"" + selection + "\" is not known.  Please try again.");
		}
	}
	
	/**
	 * We delegate the actual display and input for a menu to a later 
	 * child class.
	 * 
	 * @return The key of the selected item or null or empty string if the user
	 * simply hit return.
	 * 
	 * @throws UIException
	 */
	protected abstract String internalChoose() throws UIException;
}