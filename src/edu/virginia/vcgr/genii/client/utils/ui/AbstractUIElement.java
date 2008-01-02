package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * An abstract base implementation of a UIElement.  This class is an 
 * implementation of all common functionallity for all widgets regardless
 * of provider.
 * 
 * @author mmm2a
 */
public abstract class AbstractUIElement implements UIElement
{
	protected UIProvider _provider;
	
	/**
	 * Create a new UIElement with the given provider.
	 * 
	 * @param provider The provider responsible for this element.
	 */
	protected AbstractUIElement(UIProvider provider)
	{
		_provider = provider;
	}	
}