package edu.virginia.vcgr.genii.ui.shell;

import java.awt.event.KeyListener;

public interface InputBindings extends KeyListener
{
	public void addBindingActionListener(BindingActionListener listener);
	public void removeBindingActionListener(BindingActionListener listener);
}