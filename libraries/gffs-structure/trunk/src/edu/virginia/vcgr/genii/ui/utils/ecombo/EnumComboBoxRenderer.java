package edu.virginia.vcgr.genii.ui.utils.ecombo;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

@SuppressWarnings("rawtypes")
class EnumComboBoxRenderer<Type extends Enum<Type>> extends JLabel implements ListCellRenderer
{
	static final long serialVersionUID = 0L;

	private Map<Type, Icon> _iconMap;

	EnumComboBoxRenderer(Map<Type, Icon> iconMap)
	{
		_iconMap = iconMap;

		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setText((value == null) ? " " : value.toString());
		setFont(list.getFont());

		if (_iconMap != null)
			setIcon(_iconMap.get(value));

		return this;
	}
}