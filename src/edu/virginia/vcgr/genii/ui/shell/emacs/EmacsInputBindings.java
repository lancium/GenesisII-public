package edu.virginia.vcgr.genii.ui.shell.emacs;

import java.awt.event.KeyEvent;

import edu.virginia.vcgr.genii.ui.shell.BaseInputBindings;
import edu.virginia.vcgr.genii.ui.shell.KeySet;

public class EmacsInputBindings extends BaseInputBindings
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
				case KeyEvent.VK_B :
					fireLeft();
					break;
				case KeyEvent.VK_F :
					fireRight();
					break;
				case KeyEvent.VK_D :
					fireDelete();
					break;
				case KeyEvent.VK_C :
					fireClear();
					break;
				case KeyEvent.VK_P :
					fireBackwardHistory();
					break;
				case KeyEvent.VK_N :
					fireForwardHistory();
					break;
				case KeyEvent.VK_R :
					fireSearch();
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
				case 'b' :
					fireLeft();
					break;
				case 'f' :
					fireRight();
					break;
				case 'd' :
					fireDelete();
					break;
				case 'c' :
					fireClear();
					break;
				case 'p' :
					fireBackwardHistory();
					break;
				case 'n' :
					fireForwardHistory();
					break;
				case 'r' :
					fireSearch();
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