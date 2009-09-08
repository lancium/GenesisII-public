package edu.virginia.vcgr.genii.ui.shell;

import java.awt.event.KeyEvent;

public class DefaultInputBindings extends BaseInputBindings
{
	static final private KeySet SYMBOLS = new KeySet(
	" \t`~!@#$%^&*()_-+={}|[]\\:\";'<>?,./");
	
	@Override
	protected void keyPressed(int keyCode, KeyEvent e)
	{
		if (e.isControlDown())
		{
			switch (keyCode)
			{
				case KeyEvent.VK_C :
					fireClear();
					break;
			}
		}
	
		switch (keyCode)
		{
			case KeyEvent.VK_ESCAPE :
				fireBeep();
				break;
			case KeyEvent.VK_LEFT :
				fireLeft();
				break;
			case KeyEvent.VK_RIGHT :
				fireRight();
				break;
			case KeyEvent.VK_UP :
				fireBackwardHistory();
				break;
			case KeyEvent.VK_DOWN :
				fireForwardHistory();
				break;		
		}
		
		e.consume();
	}
	
	@Override
	protected void keyTyped(char keyChar, KeyEvent e)
	{
		if (e.isControlDown())
		{
			switch (keyChar)
			{
				case 'c' :
					fireClear();
					break;
			}
		} else
		{
			if (keyChar == KeyEvent.VK_TAB)
				fireComplete();
			else if (Character.isLetterOrDigit(keyChar) || SYMBOLS.inSet(keyChar))
				fireAddCharacter(keyChar);
			else if (keyChar == KeyEvent.VK_BACK_SPACE)
				fireBackspace();
			else if (keyChar == KeyEvent.VK_LEFT)
				fireLeft();
			else if (keyChar == KeyEvent.VK_RIGHT)
				fireRight();
			else if (keyChar == KeyEvent.VK_UP)
				fireBackwardHistory();
			else if (keyChar == KeyEvent.VK_DOWN)
				fireForwardHistory();
			else if (keyChar == KeyEvent.VK_ENTER)
				fireEnter();
		}
		
		e.consume();
	}
}