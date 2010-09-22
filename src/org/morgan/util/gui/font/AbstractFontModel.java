package org.morgan.util.gui.font;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractFontModel implements FontModel
{
	private Collection<FontListener> _listeners =
		new LinkedList<FontListener>();
	
	final protected void fireFontChanged(Font newFont)
	{
		Collection<FontListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<FontListener>(_listeners);
		}
		
		for (FontListener listener : listeners)
			listener.fontChanged(newFont);
	}
	
	protected AbstractFontModel()
	{
	}
	
	@Override
	final public void addFontListener(FontListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}

	@Override
	final public void removeFontListener(FontListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
}