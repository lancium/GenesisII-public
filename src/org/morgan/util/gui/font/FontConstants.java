package org.morgan.util.gui.font;

import java.awt.Font;

interface FontConstants
{
	static final public String DEFAULT_FONT_FAMILY = Font.DIALOG;
	static final public int DEFAULT_FONT_STYLE = Font.PLAIN;
	static final public int DEFAULT_FONT_SIZE = 12;
	
	static final public Font DEFAULT_FONT = new Font(
		DEFAULT_FONT_FAMILY, DEFAULT_FONT_STYLE, DEFAULT_FONT_SIZE);
}
