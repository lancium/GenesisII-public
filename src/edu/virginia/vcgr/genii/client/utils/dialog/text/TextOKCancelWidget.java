package edu.virginia.vcgr.genii.client.utils.dialog.text;

import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.OKCancelType;
import edu.virginia.vcgr.genii.client.utils.dialog.OKCancelWidget;

/**
 *  A text based OKCancel Widget.
 *  
 * @author mmm2a
 */
public class TextOKCancelWidget extends AbstractTextWidget implements
		OKCancelWidget
{
	private boolean _includeCancel = false;
	private TextGeneralQuestionWidget _widget;
	private OKCancelType _answer = null;
	
	/**
	 * Construct a new text based ok/cancel widget.
	 * 
	 * @param provider The provider responsible for this widget.
	 * @param title The initial title.
	 */
	public TextOKCancelWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
		
		_widget = new TextGeneralQuestionWidget(provider, title);
		_widget.setDefault(OKCancelType.OK.name());
	}
	
	@Override
	public OKCancelType getAnswer()
	{
		return _answer;
	}

	@Override
	public void includeCancel(boolean includeCancel)
	{
		_includeCancel = includeCancel;
	}

	@Override
	public void setDefault(OKCancelType defaultAnswer)
	{
		if (defaultAnswer == null)
		{
			_widget.setDefault(null);
			return;
		}
		
		if (!_includeCancel && defaultAnswer == OKCancelType.Cancel)
			throw new IllegalArgumentException(
				"Cannot set the default to cancel for a dialog that " +
				"doesn't except cancel responses.");
		
		_widget.setDefault(defaultAnswer.name());
	}

	@Override
	public void setPrompt(String prompt)
	{
		_widget.setPrompt(prompt);
	}

	@Override
	public void showWidget() throws DialogException
	{
		_answer = null;
		
		while (true)
		{
			_widget.showWidget();
			
			String answer = _widget.getAnswer();
			
			if (answer.length() == 0)
				_answer = OKCancelType.OK;
			else
			{
				if (answer.equalsIgnoreCase(OKCancelType.OK.name()))
					_answer = OKCancelType.OK;
				else
				{
					if (_includeCancel)
					{
						if (answer.equalsIgnoreCase(OKCancelType.Cancel.name()))
						{
							_answer = OKCancelType.Cancel;
							return;
						}
						
						showErrorMessage("Please select one of OK or Cancel.");
					} else
					{
						showErrorMessage("Please enter OK.");
					}
					
					continue;
				}
			}
			
			return;
		}
	}
}