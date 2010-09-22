package org.morgan.util.gui.font;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import javax.swing.AbstractListModel;

class FontFamilyListModel extends AbstractListModel
{
	static final long serialVersionUID = 0L;
	
	static private String []_sortedFontFamilies;
	
	static
	{
		GraphicsEnvironment env = 
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		_sortedFontFamilies = env.getAvailableFontFamilyNames();
		Arrays.sort(_sortedFontFamilies);
	}
	
	@Override
	final public int getSize()
	{
		return _sortedFontFamilies.length;
	}

	@Override
	final public Object getElementAt(int index)
	{
		return _sortedFontFamilies[index];
	}
}