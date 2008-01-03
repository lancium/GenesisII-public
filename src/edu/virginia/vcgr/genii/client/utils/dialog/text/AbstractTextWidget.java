package edu.virginia.vcgr.genii.client.utils.dialog.text;

import edu.virginia.vcgr.genii.client.utils.dialog.AbstractWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;

/**
 * An abstract base class for all text based widgets.
 * 
 * @author mmm2a
 */
public abstract class AbstractTextWidget extends AbstractWidget
{
	/**
	 * Construct a new text widget.
	 * 
	 * @param provider The provider that is responsible for this widget.
	 * @param title The title that the widget should have.
	 */
	protected AbstractTextWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
	}
	
	@Override
	public void showErrorMessage(String message)
	{
		TextWidgetProvider twp = TextWidgetProvider.class.cast(getProvider());
		
		twp.stderr.println(message);
	}

	@Override
	public void showErrorMessage(DialogException de)
	{
		TextWidgetProvider twp = TextWidgetProvider.class.cast(getProvider());
		de.printStackTrace(twp.stderr);
	}
}