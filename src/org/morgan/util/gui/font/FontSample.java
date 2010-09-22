package org.morgan.util.gui.font;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

class FontSample extends JLabel
{
	static final long serialVersionUID = 0L;
	
	FontSample(FontModel model)
	{
		super("Sample Text");
		
		setFont(model.selectedFont());
		
		Dimension preferredSize = getPreferredSize();
		preferredSize = new Dimension(
			preferredSize.width * 3 / 2, preferredSize.height);
		setPreferredSize(preferredSize);
		setMinimumSize(preferredSize);
		setMaximumSize(preferredSize);
		
		model.addFontListener(new InternalFontListener());
	}
	
	private class InternalFontListener implements FontListener
	{
		@Override
		public void fontChanged(Font newFont)
		{
			setFont(newFont);
			revalidate();
			repaint();
		}
	}
}