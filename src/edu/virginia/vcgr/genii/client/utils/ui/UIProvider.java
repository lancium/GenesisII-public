package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * The UI provider interface is the provider interface that ui systems give from whence
 * widgets can be created.  These widgets then act within the bounds of that ui system
 * (text, gui, etc.).
 * 
 * @author mmm2a
 */
public interface UIProvider
{
	/**
	 * A common class that represents a single menu item inside of a menu.
	 * For text based reasons, these menu items have keys associated with
	 * them though for certain UI systems such as GUI, those keys aren't
	 * necessarily displayed.
	 * 
	 * @author mmm2a
	 */
	static public class MenuElement
	{
		private String _key;
		private Object _value;
		
		/**
		 * Construct a new menu item.
		 * 
		 * @param key The key to use to identify this menu item.
		 * @param value The value of the menu item.
		 */
		public MenuElement(String key, Object value)
		{
			if (_key == null)
				throw new IllegalArgumentException("Key must be non-null.");
			
			_key = key;
			_value = value;
		}
		
		/**
		 * Retrieve the key of this menu item.
		 * @return The key of this menu item.
		 */
		public String getKey()
		{
			return _key;
		}
		
		/**
		 * Retrieve the value of this menu item.
		 * @return The value of this menu item.
		 */
		public Object getValue()
		{
			return _value;
		}
	}
	
	/**
	 * Create a new menu widget from this provider.
	 * 
	 * @param header The header (optional) to use before menu items 
	 * are displayed.
	 * @param footer The footer (or prompt) to query the user with after
	 * menu choices are displayed.  This value cannot be null.
	 * @param includeCancel A boolean indicating whether or not cancelling
	 * the menu should be a valid choice.
	 * @param elements The elements inside the menu to choose from.
	 * @return A new menu widget
	 * 
	 * @throws UIException
	 */
	public UIMenu createMenu(String header, String footer,
		boolean includeCancel, MenuElement []elements) throws UIException;
	
	/**
	 * Create a new general question widget from this provider.
	 * 
	 * @param header The header (optional) to display before the question is
	 * asked of the user.  This value can be null.
	 * @param question The question to query the user with (cannot be null).
	 * @param defaultValue A default value to use if the user simply hits
	 * return.  This value may be NULL.
	 * 
	 * @return The new GeneralQuestion widget.
	 * 
	 * @throws UIException
	 */
	public UIGeneralQuestion createGeneralQuestion(String header, 
		String question, String defaultValue) throws UIException;
	
	/**
	 * Create a yes/no/cancel question widget from this provider.
	 * 
	 * @param header The header (optional) to display before the question is
	 * asked of the user.  This value can be null.
	 * @param question The question to query the user with (cannot be null).
	 * @param defaultValue A default value to use if the user simply hits
	 * return.  This value may be NULL.
	 * 
	 * @return The new YesNoQuestion widget.
	 * 
	 * @throws UIException
	 */
	public UIYesNoQuestion createYesNoQuestion(String header, String question,
		UIYesNoCancelType defaultValue) throws UIException;
	
	/**
	 * Create a ok/cancel question widget from this provider.
	 * 
	 * @param header The header (optional) to display before the question is
	 * asked of the user.  This value can be null.
	 * @param question The question to query the user with (cannot be null).
	 * @param defaultValue A default value to use if the user simply hits
	 * return.  This value may be NULL.
	 * 
	 * @return The new OKCancelQuestion widget.
	 * 
	 * @throws UIException
	 */
	public UIOKCancelQuestion createOKCancelQuestion(String header, 
		String question, UIOKCancelType defaultValue) throws UIException;
	
	/**
	 * Create a new password widget from this provider.
	 * 
	 * @param header An optional header to display before the password prompt.
	 * @param question The password prompt to display.
	 * @return The password (possibly empty, but not null).
	 * 
	 * @throws UIException
	 */
	public UIPassword createPassword(String header, String question) 
		throws UIException;
}