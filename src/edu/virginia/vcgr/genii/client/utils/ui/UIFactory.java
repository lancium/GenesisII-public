package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * The UIFactory class is responsible for creating widgets based off of the
 * given provider.  It essentially acts as sugar coating for the method
 * interfaces for creating the various widgets.  Please see the provider
 * equivalent methods for detail descriptions of these method parameters.
 * 
 * @author mmm2a
 */
public class UIFactory
{
	private UIProvider _provider;
	
	public UIFactory(UIProvider provider)
	{
		_provider = provider;
	}
	
	public UIMenu createMenu(String header, String footer, 
		boolean includeCancel, 
		UIProvider.MenuElement...menuElements) throws UIException
	{
		return _provider.createMenu(header, footer, 
			includeCancel, menuElements);
	}
	
	public UIMenu createMenu(String header, String footer,
		UIProvider.MenuElement...menuElements) throws UIException
	{
		return createMenu(header, footer, false, menuElements);
	}
	
	public UIMenu createMenu(String header, String footer,
		boolean includeCancel, Object...menuElements) throws UIException
	{
		UIProvider.MenuElement []elements =
			new UIProvider.MenuElement[menuElements.length];
		
		for (int lcv = 0; lcv < menuElements.length; lcv++)
		{
			elements[lcv] = new UIProvider.MenuElement(
				Integer.toString(lcv), menuElements[lcv]);
		}
		
		return createMenu(header, footer, includeCancel, elements);
	}
	
	public UIMenu createMenu(String header, String footer,
		Object...menuElements) throws UIException
	{
		return createMenu(header, footer, false, menuElements);
	}
	
	public UIGeneralQuestion createGeneralQuestion(String header, 
		String question, String defaultValue) throws UIException
	{
		return _provider.createGeneralQuestion(header, question, 
			defaultValue);
	}
	
	public UIGeneralQuestion createGeneralQuestion(String question, 
		String defaultValue) throws UIException
	{
		return createGeneralQuestion(null, question, defaultValue);
	}
	
	public UIGeneralQuestion createGeneralQuestion(String question)
		throws UIException
	{
		return createGeneralQuestion(null, question, null);
	}
	
	public UIYesNoQuestion createYesNoQuestion(String header, String question,
		UIYesNoCancelType defaultValue) throws UIException
	{
		return _provider.createYesNoQuestion(header, question, defaultValue);
	}
	
	public UIYesNoQuestion createYesNoQuestion(String question, 
		UIYesNoCancelType defaultValue) throws UIException
	{
		return createYesNoQuestion(null, question, defaultValue);
	}
	
	public UIYesNoQuestion createYesNoQuestion(String question)
		throws UIException
	{
		return createYesNoQuestion(null, question, null);
	}
	
	public UIOKCancelQuestion createOKCancelQuestion(String header, 
		String question, UIOKCancelType defaultValue) throws UIException
	{
		return _provider.createOKCancelQuestion(header, question, 
			defaultValue);
	}
	
	public UIOKCancelQuestion createOKCancelQuestion(
		String question, UIOKCancelType defaultValue) throws UIException
	{
		return _provider.createOKCancelQuestion(null, question, 
			defaultValue);
	}
	
	public UIOKCancelQuestion createOKCancelQuestion(String question)
		throws UIException
	{
		return _provider.createOKCancelQuestion(null, question, null);
	}
}