package edu.virginia.vcgr.genii.client.utils.dialog.text;

import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.YesNoCancelType;
import edu.virginia.vcgr.genii.client.utils.dialog.YesNoWidget;

/**
 * A text based widget for getting yes/no/cancel answers from users.
 * 
 * @author mmm2a
 */
public class TextYesNoCancelWidget extends AbstractTextWidget implements
		YesNoWidget
{
	private boolean _includeCancel = false;
	private TextGeneralQuestionWidget _widget;
	private YesNoCancelType _answer = null;
	
	/**
	 * Construct a new yes/no/cancel widget.
	 * 
	 * @param provider The text widget provider responsible for this widget.
	 * @param title The initial title to give this widget.
	 */
	public TextYesNoCancelWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
		
		_widget = new TextGeneralQuestionWidget(provider, title);
	}
	
	@Override
	public YesNoCancelType getAnswer()
	{
		return _answer;
	}

	@Override
	public void includeCancel(boolean includeCancel)
	{
		_includeCancel = includeCancel;
	}

	@Override
	public void setDefault(YesNoCancelType defaultAnswer)
	{
		if (defaultAnswer == null)
		{
			_widget.setDefault(null);
			return;
		}
		
		if (!_includeCancel && defaultAnswer == YesNoCancelType.Cancel)
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
			
			if (answer.equalsIgnoreCase(YesNoCancelType.Yes.name()))
				_answer = YesNoCancelType.Yes;
			else if (answer.equalsIgnoreCase(YesNoCancelType.No.name()))
				_answer = YesNoCancelType.No;
			else
			{
				if (_includeCancel)
				{
					if (answer.equalsIgnoreCase(YesNoCancelType.Cancel.name()))
					{
						_answer = YesNoCancelType.Cancel;
						return;
					}
					
					showErrorMessage("Please select one of Yes, No, or Cancel.");
				} else
				{
					showErrorMessage("Please enter either Yes or No.");
				}
				
				continue;
			}
				
			return;
		}
	}
}