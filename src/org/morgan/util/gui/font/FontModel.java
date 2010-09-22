package org.morgan.util.gui.font;

import java.awt.Font;

public interface FontModel
{
	public Font selectedFont();
	
	public int styleMask();
	
	public int minimumSize();
	public int maximumSize();
	
	public void setStyle(int style);
	public void clearStyle(int style);
	
	public void setSize(int size);
	
	public void setFamily(String family);
	
	public void addFontListener(FontListener listener);
	public void removeFontListener(FontListener listener);
}