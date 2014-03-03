package edu.virginia.vcgr.genii.gjt.gui.fs;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

@SuppressWarnings("rawtypes")
class FilesystemBoxRenderer extends JLabel implements ListCellRenderer
{
	static final long serialVersionUID = 0L;

	FilesystemBoxRenderer()
	{
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component
		getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (value == null)
			value = FilesystemType.Default;

		FilesystemType filesystemType = (FilesystemType) value;

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setIcon(filesystemType.icon());
		setText(filesystemType.toString());
		setFont(list.getFont());

		return this;
	}
}