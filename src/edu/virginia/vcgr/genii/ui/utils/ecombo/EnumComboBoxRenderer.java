package edu.virginia.vcgr.genii.ui.utils.ecombo;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class EnumComboBoxRenderer
	extends JLabel implements ListCellRenderer 
{
	static final long serialVersionUID = 0L;
	
	EnumComboBoxRenderer()
	{
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

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

		setText((value == null) ? " " : value.toString());
		setFont(list.getFont());
		
		return this;
	}
}