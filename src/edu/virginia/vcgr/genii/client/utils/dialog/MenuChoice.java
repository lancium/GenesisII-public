package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A menu choice is an element in a menu that a user can select.  It bundles
 * together a key that the user can identify the choice by (which may or
 * may NOT get displayed) and the value of the choice (which get's displayed
 * using that object's toString method).
 * 
 * @author mmm2a
 */
public class MenuChoice
{
	private String _key;
	private Object _value;
	
	/**
	 * Create a new menu choice with the given key and value.
	 * 
	 * @param key The key to identify this choice by.
	 * @param value The value of the menu choice.
	 */
	public MenuChoice(String key, Object value)
	{
		_key = key;
		_value = value;
	}
	
	/**
	 * Retrieve the key for this menu choice.
	 * 
	 * @return The key for this menu choice.
	 */
	public String getKey()
	{
		return _key;
	}
	
	/**
	 * Retrieve the value for this menu choice.
	 * 
	 * @return The value for this menu choice.
	 */
	public Object getValue()
	{
		return _value;
	}
}