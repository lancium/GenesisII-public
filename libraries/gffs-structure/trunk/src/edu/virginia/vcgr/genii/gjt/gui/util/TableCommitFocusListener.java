package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class TableCommitFocusListener extends FocusAdapter
{
	private JTable _table;

	public TableCommitFocusListener(JTable table)
	{
		_table = table;
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		TableCellEditor editor = _table.getCellEditor();
		if (editor != null) {
			if (!editor.stopCellEditing()) {
				JOptionPane.showMessageDialog(_table, "Unable to commit partial change to table cell.",
					"Partial Change Failure", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
