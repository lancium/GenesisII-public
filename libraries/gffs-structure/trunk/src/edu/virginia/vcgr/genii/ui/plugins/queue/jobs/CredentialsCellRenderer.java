package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CredentialsCellRenderer extends DefaultTableCellRenderer
{
	static final long serialVersionUID = 0L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
		int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		CredentialBundle bundle = (CredentialBundle) value;

		setText(bundle.toString());
		setToolTipText(bundle.tooltipText());

		return this;
	}
}