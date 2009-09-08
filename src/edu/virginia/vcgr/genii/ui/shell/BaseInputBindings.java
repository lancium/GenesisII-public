package edu.virginia.vcgr.genii.ui.shell;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;

public class BaseInputBindings implements InputBindings
{
	private Collection<BindingActionListener> _listeners =
		new LinkedList<BindingActionListener>();

	final private BindingActionListener[] getListeners()
	{
		BindingActionListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = _listeners.toArray(
				new BindingActionListener[_listeners.size()]);
		}
		
		return listeners;
	}
	
	final protected void fireBeep()
	{
		for (BindingActionListener listener : getListeners())
			listener.beep();
	}

	final protected void fireClear()
	{
		for (BindingActionListener listener : getListeners())
			listener.clear();
	}

	final protected void fireAddCharacter(char c)
	{
		for (BindingActionListener listener : getListeners())
			listener.addCharacter(c);
	}

	final protected void fireBackspace()
	{
		for (BindingActionListener listener : getListeners())
			listener.backspace();
	}

	final protected void fireDelete()
	{
		for (BindingActionListener listener : getListeners())
			listener.delete();
	}

	final protected void fireLeft()
	{
		for (BindingActionListener listener : getListeners())
			listener.left();
	}

	final protected void fireRight()
	{
		for (BindingActionListener listener : getListeners())
			listener.right();
	}

	final protected void fireEnd()
	{
		for (BindingActionListener listener : getListeners())
			listener.end();
	}

	final protected void fireHome()
	{
		for (BindingActionListener listener : getListeners())
			listener.home();
	}

	final protected void fireBackwardHistory()
	{
		for (BindingActionListener listener : getListeners())
			listener.backwardHistory();
	}

	final protected void fireForwardHistory()
	{
		for (BindingActionListener listener : getListeners())
			listener.forwardHistory();
	}

	final protected void fireComplete()
	{
		for (BindingActionListener listener : getListeners())
			listener.complete();
	}

	final protected void fireEnter()
	{
		for (BindingActionListener listener : getListeners())
			listener.enter();
	}
	
	final protected void fireSearch()
	{
		for (BindingActionListener listener : getListeners())
			listener.search();
	}
	
	final protected void fireStopSearch()
	{
		for (BindingActionListener listener : getListeners())
			listener.stopSearch();
	}

	protected void keyPressed(int keyCode, KeyEvent e)
	{
		
	}
	
	protected void keyReleased(int keyCode, KeyEvent e)
	{
		
	}
	
	protected void keyTyped(char keyChar, KeyEvent e)
	{
		
	}
	
	@Override
	final public void addBindingActionListener(BindingActionListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}

	@Override
	final public void removeBindingActionListener(BindingActionListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}

	@Override
	final public void keyPressed(KeyEvent e)
	{
		keyPressed(e.getKeyCode(), e);
	}

	@Override
	final public void keyReleased(KeyEvent e)
	{
		keyReleased(e.getKeyCode(), e);
	}

	@Override
	final public void keyTyped(KeyEvent e)
	{
		keyTyped(e.getKeyChar(), e);
	}
}
