package edu.virginia.vcgr.genii.gjt.gui.fs;

import javax.swing.table.DefaultTableCellRenderer;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

public class FilesystemCellRenderer extends DefaultTableCellRenderer
{
	static final long serialVersionUID = 0l;

	@Override
	public void setValue(Object value)
	{
		FilesystemType fs = (FilesystemType) value;
		if (fs == null) {
			setText(null);
			setIcon(null);
		} else {
			setText(fs.toString());
			setIcon(fs.icon());
		}
	}
}