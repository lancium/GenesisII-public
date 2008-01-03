package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A widget that implements the menu interface (the user selects from a small
 * number of choices).
 * 
 * @author mmm2a
 */
public interface MenuWidget extends Widget
{
	/**
	 * The prompt to display asking the user to make a selection.
	 * 
	 * @param prompt The prompt to display.
	 */
	public void setPrompt(String prompt);
	
	/**
	 * Set the choices that this menu can display.  Calling this method
	 * clears all previous choices as well as selections already made and
	 * essentially resets the widget.
	 * 
	 * @param choices The new menu choices to include in this menu.
	 */
	public void setChoices(MenuChoice...choices);
	
	/**
	 * Similar to the setChoices method above, this setChoices method takes
	 * a list of strings to make choices from (and assigns them numeric
	 * keys).
	 * 
	 * @param choices The list of choices to set for this menu.
	 */
	public void setChoices(Object...choices);
	
	/**
	 * Retrieve the currently selected menu choice.  This method returns
	 * null until a choice is made at which point it will continue returning
	 * that choice until the menu is cleared or a new choice is made.
	 * 
	 * @return The selected menu choice (which may be null).
	 */
	public Object getSelectedChoice();
}