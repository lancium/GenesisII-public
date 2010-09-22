package org.morgan.util.gui.font;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class FontFamilyListCellRenderer extends JLabel 
	implements ListCellRenderer
{
	static final long serialVersionUID = 0L;
	
	FontFamilyListCellRenderer()
	{
		setOpaque(true);
		
		// setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
		JList list, Object value, int index, boolean isSelected,
		boolean cellHasFocus) 
	{
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else 
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setIcon(null);
		setText(value.toString());
		
		setFont(new Font(value.toString(),
			Font.PLAIN, FontConstants.DEFAULT_FONT_SIZE));
		
		return this;
	}
}