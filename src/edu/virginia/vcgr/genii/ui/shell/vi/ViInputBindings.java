package edu.virginia.vcgr.genii.ui.shell.vi;

import java.awt.event.KeyEvent;

import edu.virginia.vcgr.genii.ui.shell.BaseInputBindings;
import edu.virginia.vcgr.genii.ui.shell.KeySet;

public class ViInputBindings extends BaseInputBindings
{
	static final private KeySet SYMBOLS = new KeySet(
		" \t`~!@#$%^&*()_-+={}|[]\\:\";'<>?,./");

	private ViMode _mode = ViMode.INPUT_MODE;
	
	@Override
	protected void keyPressed(int keyCode, KeyEvent e)
	{
		if (e.isControlDown())
		{
			if (keyCode == KeyEvent.VK_C)
			{
				fireClear();
				_mode = ViMode.INPUT_MODE;
			} else if (keyCode == KeyEvent.VK_R)
			{
				fireSearch();
				_mode = ViMode.INPUT_MODE;
			}
		} else
		{
			if (_mode == ViMode.INPUT_MODE)
			{
				if (keyCode == KeyEvent.VK_ESCAPE)
				{
					fireStopSearch();
					_mode = ViMode.COMMAND_MODE;
				}
			} else if (_mode == ViMode.COMMAND_MODE)
			{
				if (keyCode == KeyEvent.VK_ESCAPE)
					fireBeep();
			}
			
			if (keyCode == KeyEvent.VK_LEFT)
				fireLeft();
			else if (keyCode == KeyEvent.VK_RIGHT)
				fireRight();
			else if (keyCode == KeyEvent.VK_UP)
				fireBackwardHistory();
			else if (keyCode == KeyEvent.VK_DOWN)
				fireForwardHistory();
		}
		
		e.consume();
	}

	@Override
	protected void keyTyped(char keyChar, KeyEvent e)
	{
		if (e.isControlDown())
		{
			if (keyChar == 'c')
			{
				fireClear();
				_mode = ViMode.INPUT_MODE;
			} else if (keyChar == 'r')
			{
				fireSearch();
				_mode = ViMode.INPUT_MODE;
			}
		} else
		{
			if (_mode == ViMode.INPUT_MODE)
			{
				if (keyChar == KeyEvent.VK_TAB)
					fireComplete();
				else if (Character.isLetterOrDigit(keyChar) || SYMBOLS.inSet(keyChar))
					fireAddCharacter(keyChar);
				else if (keyChar == KeyEvent.VK_BACK_SPACE)
					fireBackspace();
				else if (keyChar == KeyEvent.VK_ENTER)
				{
					fireEnter();
					_mode = ViMode.INPUT_MODE;
				}
			} else if (_mode == ViMode.COMMAND_MODE)
			{
				if (keyChar == KeyEvent.VK_ENTER)
					fireEnter();
				else
				{
					switch (keyChar)
					{
						case 'x' :
							fireDelete();
							break;
						case 'X' :
							fireBackspace();
							break;
						case 'h' :
							fireLeft();
							break;
						case 'l' :
							fireRight();
							break;
						case 'A' :
							fireEnd();
							_mode = ViMode.INPUT_MODE;
							break;
						case 'a' :
							fireRight();
							_mode = ViMode.INPUT_MODE;
							break;
						case 'I' :
							fireHome();
							_mode = ViMode.INPUT_MODE;
							break;
						case 'i' :
							_mode = ViMode.INPUT_MODE;
							break;
						case 'k' :
							fireBackwardHistory();
							break;
						case 'j' :
							fireForwardHistory();
							break;
					}
				}
			}
		}
		
		e.consume();
	}
}